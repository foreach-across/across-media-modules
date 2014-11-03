package com.foreach.across.modules.taskrunner.services;

import com.foreach.across.modules.taskrunner.business.PersistedTask;
import com.foreach.across.modules.taskrunner.business.Task;
import com.foreach.across.modules.taskrunner.business.TaskRequest;

/**
 * Central interface for submitting/retrieving (a)synchronous tasks.
 */
public interface TaskRunnerService
{
	Task submit( TaskRequest<?> request );

	Task execute( TaskRequest request );

	/**
	 * Retrieve the task associated with a unique id.
	 *
	 * @param id Unique id of the task.
	 * @return Task instance or null if none found.
	 */
	Task getTaskById( String id );

	PersistedTask getReportTaskById( long id );

	PersistedTask getReportTaskByUuid( String uuid );

	<T> T getReportResult( String xml, Class<T> clazz );
}
