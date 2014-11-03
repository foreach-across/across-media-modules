package com.foreach.across.modules.taskrunner.services;

public class TaskExecutionException extends RuntimeException
{
	public TaskExecutionException( String message ) {
		super( message );
	}

	public TaskExecutionException( Throwable cause ) {
		super( cause );
	}
}
