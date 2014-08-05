package com.foreach.imageserver.core.services;

public class MissingRepositoryParameterException extends RuntimeException
{
	public MissingRepositoryParameterException( String cause ) {
		super( cause );
	}
}
