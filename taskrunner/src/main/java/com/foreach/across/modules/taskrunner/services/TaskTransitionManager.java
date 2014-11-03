package com.foreach.across.modules.taskrunner.services;

import com.foreach.across.modules.taskrunner.business.Task;
import com.foreach.across.modules.taskrunner.business.TaskResult;

public interface TaskTransitionManager
{
	/**
	 * Resets the state of a task to scheduled.
	 */
	void reschedule( Task task );

	/**
	 * Puts the state to BUSY.
	 */
	void begin( Task task );

	/**
	 * Updates the timestamps.
	 */
	void touch( Task task );

	/**
	 * Puts the task in a finished state, either success or failure depending
	 * on the result.
	 */
	void finish( Task task, TaskResult result );
}
