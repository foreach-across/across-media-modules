package com.foreach.across.modules.taskrunner.tasks.reports;

public class JsonReportFileResult extends ReportFileResult
{
	public JsonReportFileResult( String name, String path) {
		super( name, path, ReportFormat.JSON );
	}
}
