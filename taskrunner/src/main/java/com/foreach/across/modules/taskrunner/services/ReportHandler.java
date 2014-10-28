package com.foreach.across.modules.taskrunner.services;

import com.foreach.across.modules.taskrunner.business.ReportRequest;
import com.foreach.across.modules.taskrunner.business.ReportResult;
import com.foreach.across.modules.taskrunner.business.ReportTask;

/**
 * Handler interface taking care of all related.
 *
 * @author Arne Vandamme
 */
public interface ReportHandler<T>
{
	/**
	 * Checks if the handler can handle this type of request.  Can look at request type
	 * or inspect the parameters.
	 *
	 * @return True if the handler can handle this type of request.
	 */
	boolean accepts( ReportRequest request );

	/**
	 * Checks if a request matches an existing task.  Does a more advanced check that can take
	 * eg the principal generating the request vs the principal of the original request into account.
	 */
	boolean matches( ReportRequest request, ReportTask task );

	/**
	 * Executes the actual task and generates the result.
	 *
	 * @return The result object of the task.  Must be serializable.
	 */
	ReportResult generate( ReportTask task, ReportRequest<T> request ) throws ReportTaskException;

	/**
	 * Remove the results of task.  Called during cleanup.
	 */
	void cleanResults( ReportTask task );

	String generateHash( ReportRequest<T> reportRequest, ReportParameterSerializer reportParameterSerializer );
}
