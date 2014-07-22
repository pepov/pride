package com.prezi.gradle.pride.cli;

import com.prezi.gradle.pride.Module;
import com.prezi.gradle.pride.Pride;
import com.prezi.gradle.pride.PrideException;
import com.prezi.gradle.pride.vcs.VcsManager;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.gradle.BasicGradleProject;
import org.gradle.tooling.model.gradle.GradleBuild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class PrideInitializer {

	private static final Logger logger = LoggerFactory.getLogger(PrideInitializer.class);
	private static final String DO_NOT_MODIFY_WARNING =
			"//\n" +
			"// DO NOT MODIFY -- This file is generated by Pride, and will be\n" +
			"// overwritten whenever the pride itself is changed.\n//\n";
	private static final ThreadLocal<GradleConnector> gradleConnector = new ThreadLocal<GradleConnector>() {
		@Override
		protected GradleConnector initialValue() {
			logger.info("Starting Gradle connector");
			return GradleConnector.newConnector();
		}
	};

	public static Pride create(final File prideDirectory, Configuration configuration, VcsManager vcsManager) throws IOException {
		logger.info("Initializing " + prideDirectory);
		prideDirectory.mkdirs();

		File configDirectory = Pride.getPrideConfigDirectory(prideDirectory);
		FileUtils.deleteDirectory(configDirectory);
		configDirectory.mkdirs();
		FileUtils.write(Pride.getPrideVersionFile(configDirectory), "0\n");
		Pride.getPrideModulesFile(configDirectory).createNewFile();

		Pride pride = Pride.getPride(prideDirectory, configuration, vcsManager);
		reinitialize(pride);
		return pride;
	}

	public static void reinitialize(final Pride pride) throws IOException {
		File buildFile = pride.gradleBuildFile;
		buildFile.delete();
		FileUtils.write(buildFile, DO_NOT_MODIFY_WARNING);
		FileOutputStream buildOut = new FileOutputStream(buildFile, true);
		try {
			IOUtils.copy(PrideInitializer.class.getResourceAsStream("/build.gradle"), buildOut);
		} finally {
			buildOut.close();
		}

		final File settingsFile = pride.gradleSettingsFile;
		settingsFile.delete();
		FileUtils.write(settingsFile, DO_NOT_MODIFY_WARNING);
		for (Module module : pride.getModules()) {
			File moduleDirectory = new File(pride.rootDirectory, module.getName());
			if (Pride.isValidModuleDirectory(moduleDirectory)) {
				PrideInitializer.initializeModule(pride, moduleDirectory, settingsFile);
			}
		}
	}

	private static void initializeModule(Pride pride, final File moduleDirectory, final File settingsFile) {
		ProjectConnection connection = gradleConnector.get().forProjectDirectory(moduleDirectory).connect();
		try {
			final String relativePath = pride.rootDirectory.toURI().relativize(moduleDirectory.toURI()).toString();

			// Load the model for the build
			ModelBuilder<GradleBuild> builder = connection.model(GradleBuild.class);
			// TODO Use '-s' in verbose mode
			builder.withArguments("-q");
			final GradleBuild build = builder.get();

			// Merge settings
			FileUtils.write(settingsFile, "\n// Settings from project in directory /" + relativePath + "\n\n", true);
			for (BasicGradleProject project : build.getProjects()) {
				if (project.equals(build.getRootProject())) {
					FileUtils.write(settingsFile, "include \'" + build.getRootProject().getName() + "\'\n", true);
					FileUtils.write(settingsFile, "project(\':" + build.getRootProject().getName() + "\').projectDir = file(\'" + moduleDirectory.getName() + "\')\n", true);
				} else {
					FileUtils.write(settingsFile, "include \'" + build.getRootProject().getName() + project.getPath() + "\'\n", true);
				}
			}
		} catch (Exception ex) {
			throw new PrideException("Could not parse module in " + moduleDirectory + ": " + ex, ex);
		} finally {
			// Clean up
			connection.close();
		}
	}

	public static void refreshDependencies(Pride pride) {
		logger.info("Refreshing dependencies");
		ProjectConnection connection = gradleConnector.get().forProjectDirectory(pride.rootDirectory).connect();
		connection.newBuild()
				.forTasks("doNothing")
				.withArguments("--refresh-dependencies")
				.run();
	}
}
