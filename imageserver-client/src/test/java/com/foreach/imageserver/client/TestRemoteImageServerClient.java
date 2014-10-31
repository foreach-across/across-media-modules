package com.foreach.imageserver.client;

import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageTypeDto;
import com.foreach.imageserver.dto.ImageVariantDto;
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

	@Test
	public void imageUrlWithZeroHeight() {
		String url = imageServerClient.imageUrl( "10", "ONLINE", 1000, 0, ImageTypeDto.TIFF );
		assertEquals( "http://localhost:8078/view?iid=10&context=ONLINE&width=1000&imageType=TIFF", url );
	}

	@Test
	public void imageUrlWithZeroWidth() {
		String url = imageServerClient.imageUrl( "10", "ONLINE", 0, 2000 );
		assertEquals( "http://localhost:8078/view?iid=10&context=ONLINE&height=2000", url );
	}

	@Test (expected =  IllegalArgumentException.class)
	public void imageUrlNoSizeNoImageResolution() {
		ImageVariantDto variant = new ImageVariantDto();
		variant.setImageType( ImageTypeDto.JPEG );
		String url = imageServerClient.imageUrl( "10", "ONLINE", null, variant , null);
		assertEquals( "http://localhost:8078/view?iid=10&context=ONLINE&height=2000", url );
	}

	@Test
	public void imageUrlWithOneSize() {
		ImageVariantDto variant = new ImageVariantDto();
		variant.setImageType( ImageTypeDto.JPEG );
		String url = imageServerClient.imageUrl( "10", "ONLINE", "100x200");
		assertEquals( "http://localhost:8078/view?iid=10&context=ONLINE&size=100x200", url );
	}

	@Test
	public void imageUrlWithManySizes() {
		ImageVariantDto variant = new ImageVariantDto();
		variant.setImageType( ImageTypeDto.JPEG );
		String url = imageServerClient.imageUrl( "10", "ONLINE", "100x200", "200x400", "400x800");
		assertEquals( "http://localhost:8078/view?iid=10&context=ONLINE&size=100x200,200x400,400x800", url );
	}

	@Test
	public void imageUrlWithBoundingBox() {
		String url = imageServerClient.imageUrl( "10", "ONLINE", 1000, 2000, new ImageVariantDto( new DimensionsDto( 400, 800 ) ) );
		assertEquals( "http://localhost:8078/view?iid=10&context=ONLINE&width=1000&height=2000&boundingBox.width=400&boundingBox.height=800", url );
	}
}
