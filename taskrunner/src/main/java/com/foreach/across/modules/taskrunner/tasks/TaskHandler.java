package com.foreach.across.modules.taskrunner.tasks;

import com.foreach.across.modules.taskrunner.business.Task;
import com.foreach.across.modules.taskrunner.business.TaskRequest;
import com.foreach.across.modules.taskrunner.business.TaskResult;
import com.foreach.across.modules.taskrunner.services.TaskExecutionException;
import com.foreach.across.modules.taskrunner.tasks.reports.ReportResult;

/**
 * Handler interface used to define implementations that can handle certain tasks.
 *
 * @author Arne Vandamme
 */
public interface TaskHandler<P>
{
	/**
	 * Checks if the handler can handle tasks with the given parameter types.
	 *
	 * @return True if the handler can handle this type of task parameters.
	 */
	boolean accepts( P parameters );

	/**
	 * Checks if a request matches an existing task and the existing task can be re-used for
	 * the request.  The task would have been selected as matching the request based on the
	 * hash of the parameters and default property comparison.
	 * <p/>
	 * This method can be used to do a more in-depth check, for example comparing the principal generating
	 * the request vs the principal of the original request.
	 */
	boolean matches( TaskRequest request, Task<P> task );

	/**
	 * Executes the actual task and generates the result.
	 *
	 * @return The result object of the task.  Must be serializable.
	 */
	TaskResult execute( Task<P> task ) throws TaskExecutionException;

	/**
	 * Remove the results of task.  Called during cleanup.
	 */
	void cleanResults( Task<P> task );
}
