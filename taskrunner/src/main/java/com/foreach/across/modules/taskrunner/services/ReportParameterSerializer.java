package com.foreach.across.modules.taskrunner.services;

import com.thoughtworks.xstream.XStream;

public class ReportParameterSerializer
{
	private XStream xStream = new XStream();

	public String serialize( Object o ) {
		return xStream.toXML( o );
	}

	public <T> T deserialize( String xml, Class<T> clazz ) {
		return clazz.cast( xStream.fromXML( xml ) );
	}
}
