package com.foreach.across.modules.taskrunner.config;

import com.foreach.across.modules.taskrunner.repositories.TaskRepository;
import com.foreach.across.modules.taskrunner.repositories.TaskRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskRunnerRepositoriesConfiguration
{
	@Bean
	public TaskRepository reportTaskRepository() {
		return new TaskRepositoryImpl();
	}
}
