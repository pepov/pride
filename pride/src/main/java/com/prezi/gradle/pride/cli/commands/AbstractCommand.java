package com.prezi.gradle.pride.cli.commands;

import com.prezi.gradle.pride.PrideException;
import com.prezi.gradle.pride.vcs.VcsManager;
import io.airlift.command.Option;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.Callable;

public abstract class AbstractCommand implements Callable<Integer> {
	protected static final Logger logger = LoggerFactory.getLogger(AbstractCommand.class);

	@Option(name = {"-v", "--verbose"},
			description = "Verbose mode")
	private boolean verbose;

	@Option(name = {"-q", "--quiet"},
			description = "Quite mode")
	private boolean quiet;

	@Option(name = {"-p", "--pride-directory"},
			title = "directory",
			description = "Initializes the pride in the given directory instead of the current directory")
	private File explicitPrideDirectory;
	private VcsManager vcsManager;

	static PropertiesConfiguration loadGlobalConfiguration() {
		File configFile = new File(System.getProperty("user.home") + "/.prideconfig");
		try {
			if (!configFile.exists()) {
				FileUtils.forceMkdir(configFile.getParentFile());
				//noinspection ResultOfMethodCallIgnored
				configFile.createNewFile();
			}

			return new PropertiesConfiguration(configFile);
		} catch (Exception ex) {
			throw new PrideException("Couldn't load configuration file: " + configFile, ex);
		}
	}

	protected File getPrideDirectory() {
		File directory = explicitPrideDirectory;
		return directory != null ? directory : new File(System.getProperty("user.dir"));
	}

	protected VcsManager getVcsManager() {
		if (vcsManager == null) {
			vcsManager = new VcsManager();
		}
		return vcsManager;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public boolean isQuiet() {
		return quiet;
	}
}
