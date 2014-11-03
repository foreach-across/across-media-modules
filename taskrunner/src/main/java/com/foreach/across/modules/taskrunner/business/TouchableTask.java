package com.foreach.across.modules.taskrunner.business;

/**
 * Represents a task that can be touched.  Touching a task means the updated
 * timestamp will get modified.  This can be useful in long running task scenario's
 * where
 *
 * @author Arne Vandamme
 */
public interface TouchableTask<P> extends Task<P>
{
	/**
	 * Update the timestamp when the
	 */
	void touch();
}
