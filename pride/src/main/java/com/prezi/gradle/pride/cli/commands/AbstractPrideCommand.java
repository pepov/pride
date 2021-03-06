package com.prezi.gradle.pride.cli.commands;

import com.prezi.gradle.pride.Pride;
import com.prezi.gradle.pride.RuntimeConfiguration;

public abstract class AbstractPrideCommand extends AbstractConfiguredCommand {
	@Override
	final protected int executeWithConfiguration(RuntimeConfiguration globalConfig) throws Exception {
		Pride pride = Pride.getPride(getPrideDirectory(), globalConfig, getVcsManager());
		executeInPride(pride);
		return 0;
	}

	public abstract void executeInPride(Pride pride) throws Exception;
}
