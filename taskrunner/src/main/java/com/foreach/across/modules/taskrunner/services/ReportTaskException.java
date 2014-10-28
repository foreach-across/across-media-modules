package com.foreach.across.modules.taskrunner.services;

public class ReportTaskException extends Exception
{
	public ReportTaskException(Exception e) {
		super();
		initCause( e );
	}
}
