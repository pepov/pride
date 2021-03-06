package com.prezi.gradle.pride.cli;

import org.apache.commons.configuration.MapConfiguration;

import java.util.LinkedHashMap;

public interface Configurations {
	public static final String PRIDE_HOME = "pride.home";
	public static final String REPO_TYPE_DEFAULT = "repo.type.default";
	public static final String REPO_BASE_URL = "repo.base.url";
	public static final String REPO_CACHE_ALWAYS = "repo.cache.always";
	public static final String REPO_RECURSIVE = "repo.recursive.always";
	public static final String COMMAND_UPDATE_REFRESH_DEPENDENCIES = "command.update.refresh_dependencies.always";
	public static final String GRADLE_VERSION = "gradle.version";
	public static final String GRADLE_HOME = "gradle.home";
	public static final String GRADLE_WRAPPER = "gradle.wrapper";

	static class Defaults extends MapConfiguration {
		public Defaults() {
			super(new LinkedHashMap<String, Object>());
			final String property = System.getProperty("PRIDE_HOME");
			setProperty(PRIDE_HOME, property != null ? property : System.getProperty("user.home") + "/.pride");
			setProperty(REPO_TYPE_DEFAULT, "git");
			setProperty(REPO_CACHE_ALWAYS, true);
			setProperty(REPO_RECURSIVE, false);
			setProperty(COMMAND_UPDATE_REFRESH_DEPENDENCIES, false);
			setProperty(GRADLE_VERSION, null);
			setProperty(GRADLE_HOME, null);
			setProperty(GRADLE_WRAPPER, true);
		}
	}
}
