package com.foreach.across.modules.taskrunner.business;

/**
 * @author Arne Vandamme
 */
public enum ReportStatus
{
	/**
	 * Task has been created, but no generator has picked it up yet.
	 * Only relevant in case of a separate generator application strategy.
	 */
	SCHEDULED,

	/**
	 * Report is currently being generated.
	 */
	BUSY,

	/**
	 * Report generation has failed.  Results <u>might</u> contain exception data.
	 */
	FAILED,

	/**
	 * Report generation has finished.  Results should contain the data according to the original request.
	 */
	FINISHED
}
