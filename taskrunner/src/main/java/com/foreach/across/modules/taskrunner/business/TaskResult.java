package com.foreach.across.modules.taskrunner.business;

public class TaskResult
{
	private boolean failed;
	private Object data;
	private Throwable failureReason;

	public boolean isFailed() {
		return failed;
	}

	public void setFailed( boolean failed ) {
		this.failed = failed;
	}

	public Object getData() {
		return data;
	}

	public void setData( Object data ) {
		this.data = data;
	}

	public Throwable getFailureReason() {
		return failureReason;
	}

	public void setFailureReason( Throwable failureReason ) {
		this.failureReason = failureReason;
	}

	public static TaskResult failure( Throwable reason ) {
		TaskResult result = new TaskResult();
		result.setFailed( true );
		result.setFailureReason( reason );

		return result;
	}

	public static TaskResult success( Object data ) {
		TaskResult result = new TaskResult();
		result.setFailed( false );
		result.setData( data );

		return result;
	}
}
