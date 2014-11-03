package com.foreach.across.modules.taskrunner.services;

import com.foreach.across.modules.taskrunner.business.PersistedTask;
import com.foreach.across.modules.taskrunner.tasks.reports.ReportFileResult;
import com.foreach.across.modules.taskrunner.tasks.reports.ReportFormat;
import com.foreach.across.modules.taskrunner.tasks.reports.ReportResult;

import java.io.IOException;
import java.io.InputStream;

public interface FileManagerService
{
	ReportResult getReportResult( Object object, ReportFormat format ) throws IOException, IllegalArgumentException;

	InputStream readReportResult( ReportFileResult reportFileResult, PersistedTask reportTask );
}
