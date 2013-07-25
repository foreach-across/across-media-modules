package com.foreach.imageserver.services.repositories;

import com.foreach.imageserver.business.ImageType;
import com.foreach.imageserver.business.Dimensions;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.UnknownHostException;

/**
 * Will fetch an image from a specific url.
 */
@Service
public class HttpImageLookupRepository implements ImageLookupRepository
{
	private static final Logger LOG = LoggerFactory.getLogger( HttpImageLookupRepository.class );

	public RepositoryLookupResult fetchImage( String uri ) {
		RepositoryLookupResult result = new RepositoryLookupResult();

		try {
			LOG.info( "Fetching remote image with url " + uri );

			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet( uri );

			HttpResponse response = httpClient.execute( httpGet );
			HttpEntity entity = response.getEntity();

			result.setStatus( RepositoryLookupStatus.getForHttpStatusCode( response.getStatusLine().getStatusCode() ) );

			if ( result.getStatus() == RepositoryLookupStatus.SUCCESS ) {
				ImageType imageType = ImageType.getForContentType( entity.getContentType().getValue() );

				if ( imageType == null ) {
					throw new RuntimeException( "Unknown Content-Type: " + entity.getContentType() );
				}
				result.setImageType( imageType );
				result.setContent( entity.getContent() );
			}
		}
		catch ( UnknownHostException uhe ) {
			LOG.error( "Could not fetch image from " + uri, uhe );

			result.setStatus( RepositoryLookupStatus.NOT_FOUND );
		}
		catch ( Exception e ) {
			LOG.error( "Exception fetching image from " + uri, e );

			result.setStatus( RepositoryLookupStatus.ERROR );
		}

		return result;
	}
}
