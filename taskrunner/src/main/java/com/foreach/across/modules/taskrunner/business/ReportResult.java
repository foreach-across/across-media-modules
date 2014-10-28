package com.foreach.across.modules.taskrunner.business;

import java.util.ArrayList;
import java.util.Collection;

public class ReportResult
{
	private Collection<ReportFileResult> reportFileResults;

	public ReportResult() {
		this.reportFileResults = new ArrayList<>(  );
	}

	public Collection<ReportFileResult> getReportFileResults() {
		return reportFileResults;
	}

	public void setReportFileResults( Collection<ReportFileResult> reportFileResults ) {
		this.reportFileResults = reportFileResults;
	}

	public void addReportFileResult( ReportFileResult reportFileResult ) {
		reportFileResults.add( reportFileResult );
	}
}
