package com.foreach.across.modules.taskrunner.services;

import com.thoughtworks.xstream.XStream;

/**
 * Serializer used for (de-)serialization of task parameters, results and callbacks.
 */
public class XstreamTaskObjectsSerializer implements TaskObjectsSerializer
{
	private XStream xStream = new XStream();

	@Override
	public String serialize( Object o ) {
		return xStream.toXML( o );
	}

	@Override
	public Object deserialize( String xml ) {
		return xStream.fromXML( xml );
	}

	@Deprecated
	public <T> T deserialize( String xml, Class<T> clazz ) {
		return clazz.cast( xStream.fromXML( xml ) );
	}
}
