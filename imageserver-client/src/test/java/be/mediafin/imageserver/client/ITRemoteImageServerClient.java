package be.mediafin.imageserver.client;

import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.dto.ImageTypeDto;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Arne Vandamme
 */
public class ITRemoteImageServerClient
{
	private ImageServerClient imageServerClient = new RemoteImageServerClient( "http://localhost:8078/", "azerty" );

	@Test
	public void imageUrl() {
		String url = imageServerClient.imageUrl( "10", "ONLINE", 1000, 2000, ImageTypeDto.TIFF );
		assertEquals( "http://localhost:8078/view?iid=10&context=ONLINE&width=1000&height=2000&imageType=TIFF", url );

		url = imageServerClient.imageUrl( "10", "DIGITAL", 0, 2000, ImageTypeDto.TIFF );
		assertEquals( "http://localhost:8078/view?iid=10&context=DIGITAL&height=2000&imageType=TIFF", url );

		url = imageServerClient.imageUrl( "someid", "SITE", 1000, 0, ImageTypeDto.TIFF );
		assertEquals( "http://localhost:8078/view?iid=someid&context=SITE&width=1000&imageType=TIFF", url );
	}

	@Test
	public void listAllowedResolutions() {
		List<ImageResolutionDto> resolutions = imageServerClient.listAllowedResolutions( "online" );
		assertEquals( 28, resolutions.size() );

		assertEquals( 1000, resolutions.get( 0 ).getWidth() );
		assertEquals( 1000, resolutions.get( 0 ).getHeight() );

		assertEquals( 1000, resolutions.get( 1 ).getWidth() );
		assertNull( resolutions.get( 1 ).getHeight() );

		assertNull( resolutions.get( 2 ).getWidth() );
		assertEquals( 1000, resolutions.get( 2 ).getHeight() );
	}
}