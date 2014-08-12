package com.foreach.imageserver.client;

import com.foreach.imageserver.dto.ImageTypeDto;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Arne Vandamme
 */
public class TestRemoteImageServerClient
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
	public void imageUrlWithOutImageTypeWillReturnImageTypeOfOriginal() {
		String url = imageServerClient.imageUrl( "10", "ONLINE", 1000, 2000 );
		assertEquals( "http://localhost:8078/view?iid=10&context=ONLINE&width=1000&height=2000", url );
	}
}
