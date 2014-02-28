package com.foreach.imageserver.core.services.repositories;

import javax.servlet.http.HttpServletResponse;

public enum RepositoryLookupStatus
{
	SUCCESS,
	NOT_FOUND,
	ACCESS_DENIED,
	ERROR;

	public static RepositoryLookupStatus getForHttpStatusCode( int httpStatusCode ) {
		switch ( httpStatusCode ) {
			case HttpServletResponse.SC_OK:
				return SUCCESS;
			case HttpServletResponse.SC_NOT_FOUND:
				return NOT_FOUND;
			case HttpServletResponse.SC_FORBIDDEN:
			case HttpServletResponse.SC_UNAUTHORIZED:
				return ACCESS_DENIED;
			default:
				return ERROR;
		}
	}
}
