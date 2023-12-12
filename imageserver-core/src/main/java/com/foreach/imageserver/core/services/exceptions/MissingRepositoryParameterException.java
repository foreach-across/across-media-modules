package com.foreach.imageserver.core.services.exceptions;

public class MissingRepositoryParameterException extends RuntimeException
{
	public MissingRepositoryParameterException( String cause ) {
		super( cause );
	}
}
