package com.foreach.across.modules.taskrunner.services;

import com.foreach.across.modules.taskrunner.business.ReportRequest;
import com.foreach.across.modules.taskrunner.business.ReportTask;

public interface ReportService {
	ReportTask submit( ReportRequest<?> request );

	ReportTask execute( ReportRequest request );

	ReportTask getReportTaskById( long id );

	ReportTask getReportTaskByUuid( String uuid );

	<T> T getReportResult( String xml, Class<T> clazz );
}
