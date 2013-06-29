package com.foreach.imageserver.services.repositories;

import com.foreach.imageserver.business.ImageType;
import com.foreach.imageserver.business.image.Dimensions;
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

				BufferedImage image = ImageIO.read( entity.getContent() );
				result.setDimensions( new Dimensions( image.getWidth(), image.getHeight() ) );
				result.setImageType( imageType );
				result.setContent( entity.getContent() );
			}
		}
		catch ( Exception e ) {
			LOG.error( "Exception fetching image: ", e );

			result.setStatus( RepositoryLookupStatus.ERROR );
		}

		return result;
	}
}
