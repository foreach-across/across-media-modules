package com.foreach.imageserver.connectors.dpp;

import com.foreach.imageserver.connectors.dpp.ws.WebServiceResult;

public class AssetConversionServiceResponse implements WebServiceResult
{
	private byte[] response;
	private String key;

	public AssetConversionServiceResponse( byte[] response ) {
		this.response = response;
	}

	public byte[] getResponse() {
		return response;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public void setKey( String key ) {
		this.key = key;
	}

	@Override
	public void persist() {
		// do nothing
	}

}
