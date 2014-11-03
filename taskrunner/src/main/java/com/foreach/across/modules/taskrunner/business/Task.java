package com.foreach.across.modules.taskrunner.business;

import java.util.Date;

/**
 * @author Arne Vandamme
 */
public interface Task<P>
{
	/**
	 * @return Unique id of the task.
	 */
	String getId();

	/**
	 * @return The principal that created the original task.
	 */
	String getCreatedBy();

	/**
	 * @return The original task parameters (also the descriptor of the task).
	 */
	<T extends P> T getParameters();

	/**
	 * @return The result attached to this task.  Can be null.
	 */
	<R> R getResult();

	/**
	 * @return Current status of the task.
	 */
	TaskStatus getStatus();

	/**
	 * @return Timestamp when this task was originally created.
	 */
	Date getCreated();

	/**
	 * @return Timestamp of the last update (status change).
	 */
	Date getUpdated();

	/**
	 * @return Date at which time the task result expires.  Null if it never expires.
	 */
	Date getExpiryDate();
}
