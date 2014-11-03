package com.foreach.across.modules.taskrunner.tasks;

/**
 * Interface that can be added to customize hash generation for task parameters.
 * Usually added to the TaskHandler if it wants to provide custom parameter hashing.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.modules.taskrunner.tasks.TaskHandler
 */
public interface TaskParametersHashGenerator<P>
{
	/**
	 * Generate a unique hash of the parameters.  This hash will be used for caching and
	 * reusing task results.
	 *
	 * @param parameters Parameters object.
	 * @return String based hash.
	 */
	String generateParametersHash( P parameters );
}
