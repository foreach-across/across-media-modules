package com.foreach.across.modules.spring.batch;

import com.foreach.across.core.AcrossModuleSettings;
import com.foreach.across.core.AcrossModuleSettingsRegistry;
import org.springframework.core.task.TaskExecutor;

public class SpringBatchModuleSettings extends AcrossModuleSettings
{
	public static final String SPRING_BATCH_TASK_EXECUTOR = "springBatch.taskExecutor";

	@Override
	protected void registerSettings( AcrossModuleSettingsRegistry registry ) {
		registry.register( SPRING_BATCH_TASK_EXECUTOR, TaskExecutor.class, null,
		                   "The custom task executor to use for batch jobs.  If none specified a default will be used." );
	}

	public TaskExecutor getTaskExecutor() {
		return getProperty( SPRING_BATCH_TASK_EXECUTOR, TaskExecutor.class );
	}
}
