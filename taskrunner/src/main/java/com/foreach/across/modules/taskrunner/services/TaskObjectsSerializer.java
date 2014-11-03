package com.foreach.across.modules.taskrunner.services;

/**
 * @author Arne Vandamme
 */
public interface TaskObjectsSerializer
{
	String serialize( Object o );

	Object deserialize( String xml );
}
