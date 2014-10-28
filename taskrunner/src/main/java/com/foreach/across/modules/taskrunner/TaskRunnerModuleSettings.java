package com.foreach.across.modules.taskrunner;

import com.foreach.across.core.AcrossModuleSettings;
import com.foreach.across.core.AcrossModuleSettingsRegistry;

public class TaskRunnerModuleSettings extends AcrossModuleSettings
{
	/**
	 * Configure the minimum size of the threadpool of report generators.
	 * <p/>
	 * Value: integer (default: 10)
	 */
	public static final String GENERATOR_THREADPOOL_MINIMUM_SIZE = "acrossTaskRunner.generatorThreadpoolMinimumSize";

	/**
	 * Configure the maximum size of the threadpool of report generators.
	 * <p/>
	 * Value: integer (default: 20)
	 */
	public static final String GENERATOR_THREADPOOL_MAXIMUM_SIZE = "acrossTaskRunner.generatorThreadpoolMaximumSize";

	@Override
	protected void registerSettings( AcrossModuleSettingsRegistry registry ) {
		registry.register( GENERATOR_THREADPOOL_MINIMUM_SIZE, Integer.class, 10,
		                   "The minimum size of the threadpool of task executors." );
		registry.register( GENERATOR_THREADPOOL_MAXIMUM_SIZE, Integer.class, 20,
		                   "The maximum size of the threadpool of task executors." );
	}

	public int getGeneratorThreadpoolMinimumSize() {
		return getProperty( GENERATOR_THREADPOOL_MINIMUM_SIZE, Integer.class );
	}

	public int getGeneratorThreadpoolMaximumSize() {
		return getProperty( GENERATOR_THREADPOOL_MAXIMUM_SIZE, Integer.class );
	}
}
