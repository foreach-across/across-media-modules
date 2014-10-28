package com.foreach.across.modules.taskrunner.config;

import com.foreach.across.modules.taskrunner.TaskRunnerModuleSettings;
import com.foreach.across.modules.taskrunner.services.FileManagerService;
import com.foreach.across.modules.taskrunner.services.FileManagerServiceImpl;
import com.foreach.across.modules.taskrunner.services.ReportService;
import com.foreach.across.modules.taskrunner.services.ReportServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskRunnerServicesConfiguration
{
	@Autowired
	private TaskRunnerModuleSettings taskRunnerModuleSettings;

	@Bean
	public ReportService taskService() {
		return new ReportServiceImpl( taskRunnerModuleSettings.getGeneratorThreadpoolMinimumSize(),
		                              taskRunnerModuleSettings.getGeneratorThreadpoolMaximumSize() );
	}

	@Bean
	public FileManagerService fileManager() {
		return new FileManagerServiceImpl();
	}
}
