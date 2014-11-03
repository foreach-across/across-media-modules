package com.foreach.across.modules.taskrunner.config;

import com.foreach.across.modules.taskrunner.TaskRunnerModuleSettings;
import com.foreach.across.modules.taskrunner.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskRunnerServicesConfiguration
{
	@Autowired
	private TaskRunnerModuleSettings taskRunnerModuleSettings;

	@Bean
	public TaskRunnerService taskService() {
		return new TaskRunnerServiceImpl( taskRunnerModuleSettings.getGeneratorThreadpoolMinimumSize(),
		                              taskRunnerModuleSettings.getGeneratorThreadpoolMaximumSize() );
	}

	@Bean
	public TaskObjectsSerializer taskObjectsSerializer() {
		return new XstreamTaskObjectsSerializer();
	}

	@Bean
	public FileManagerService fileManager() {
		return new FileManagerServiceImpl();
	}
}
