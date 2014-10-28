package com.foreach.across.modules.taskrunner.services;

import com.foreach.across.modules.taskrunner.business.ReportFileResult;
import com.foreach.across.modules.taskrunner.business.ReportFormat;
import com.foreach.across.modules.taskrunner.business.ReportResult;
import com.foreach.across.modules.taskrunner.business.ReportTask;

import java.io.IOException;
import java.io.InputStream;

public interface FileManagerService
{
	ReportResult getReportResult( Object object, ReportFormat format ) throws IOException, IllegalArgumentException;

	InputStream readReportResult( ReportFileResult reportFileResult, ReportTask reportTask );
}
