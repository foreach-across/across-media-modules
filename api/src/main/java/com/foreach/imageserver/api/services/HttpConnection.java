package com.foreach.imageserver.api.services;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public abstract class HttpConnection
{
	protected final HttpResponse request( String url, HttpEntity entity ) throws IOException
	{
		HttpClient httpClient = new DefaultHttpClient();

		try {
			HttpPost httpPost = new HttpPost( url );
			httpPost.setEntity( entity );
			return httpClient.execute( httpPost );
		}
		finally {
			try {
				httpClient.getConnectionManager().shutdown();
			}
			catch ( Exception ignore ) {

			}
		}
	}

}
