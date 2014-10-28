package com.foreach.across.modules.taskrunner.services;

import com.foreach.across.modules.taskrunner.business.ReportRequest;
import org.apache.commons.codec.digest.DigestUtils;

public abstract class AbstractReportHandler<T> implements ReportHandler<T>
{
	@Override
	public String generateHash( ReportRequest<T> reportRequest, ReportParameterSerializer reportParameterSerializer ) {
		String hash = generateHandlerHash( reportRequest.getParameters(), reportParameterSerializer );
		return DigestUtils.sha256Hex( hash );
	}

	protected String generateHandlerHash( T parameters, ReportParameterSerializer reportParameterSerializer ) {
		return reportParameterSerializer.serialize( parameters );
	}
}
