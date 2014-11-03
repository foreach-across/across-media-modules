package com.foreach.across.modules.taskrunner.services;

import com.foreach.across.modules.taskrunner.business.Task;
import com.foreach.across.modules.taskrunner.business.TaskRequest;

/**
 * Central interface for submitting/retrieving (a)synchronous tasks.
 */
public interface TaskRunnerService
{
	/**
	 * Submit a task for asynchronous execution.
	 *
	 * @param request Request for the task.
	 * @return Task instance.
	 */
	Task submit( TaskRequest<?> request );

	/**
	 * Submit a task for synchronous execution, the method call
	 * will only return once the task has finished.
	 *
	 * @param request Request for the task.
	 * @return Task instance - containing the result.
	 */
	Task execute( TaskRequest request );

	/**
	 * Retrieve the task associated with a unique id.
	 *
	 * @param id Unique id of the task.
	 * @return Task instance or null if none found.
	 */
	Task getTaskById( String id );
}
