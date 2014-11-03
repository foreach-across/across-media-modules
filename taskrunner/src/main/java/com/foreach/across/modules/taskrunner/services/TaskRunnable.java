package com.foreach.across.modules.taskrunner.services;

import com.foreach.across.modules.taskrunner.business.MutableTask;
import com.foreach.across.modules.taskrunner.business.TaskResult;
import com.foreach.across.modules.taskrunner.business.TouchableTask;
import com.foreach.across.modules.taskrunner.tasks.TaskHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskRunnable<T> implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger( TaskRunnable.class );

	private final TaskHandler<T> taskHandler;
	private final MutableTask<T> task;

	private final TaskTransitionManager transitionManager;

	public TaskRunnable(
			TaskTransitionManager transitionManager, TaskHandler<T> taskHandler, MutableTask<T> task ) {
		this.transitionManager = transitionManager;
		this.taskHandler = taskHandler;
		this.task = task;
	}

	@Override
	public void run() {
		transitionManager.begin( task );

		TaskResult result;

		try {
			result = taskHandler.execute( new TouchableTaskWrapper<>( transitionManager, task ) );
		}
		catch ( Exception e ) {
			LOG.error( "Exception when executing task", e );
			result = TaskResult.failure( e );
		}

		transitionManager.finish( task, result );
	}
}
