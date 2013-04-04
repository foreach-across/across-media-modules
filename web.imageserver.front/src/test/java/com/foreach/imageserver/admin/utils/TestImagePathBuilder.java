package com.foreach.imageserver.admin.utils;

import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.services.paths.ImagePathBuilder;
import com.foreach.imageserver.services.paths.ImagePathBuilderImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestImagePathBuilder
{
	private ImagePathBuilder builder;

	@Before
	public void setup()
	{
		builder = new ImagePathBuilderImpl();
	}

	@Test
	public void invertRemoteId()
	{
		ServableImageData image = new ServableImageData();

		long imageId = 1001L;

		image.setId( imageId );
		image.setApplicationId( 1 );
		image.setGroupId( 2 );
		image.setExtension( "pqr" );
		image.setPath( "foo/bar/boz" );

		String remoteId = builder.createRemoteId( image );

		assertEquals( imageId, builder.imageIdFromRemoteId( remoteId ) );
	}
}
