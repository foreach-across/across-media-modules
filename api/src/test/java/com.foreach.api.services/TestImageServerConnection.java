package com.foreach.api.services;

import static org.junit.Assert.*;

import com.foreach.imageserver.api.services.ImageServerConnection;
import com.foreach.imageserver.api.services.ImageServerConnectionImpl;
import org.junit.Before;
import org.junit.Test;

public class TestImageServerConnection
{
	private ImageServerConnection connection;

	private int applicationId;
	private int groupId;

	private String repositoryUrl;

	@Before
	public void setup()
	{
		applicationId = 1001;
		groupId = 2002;
		repositoryUrl = "http://foo.bar.zod/boink";

		connection = new ImageServerConnectionImpl( repositoryUrl, applicationId, groupId );
	}

	@Test
	public void buildUrl()
	{
		String url = connection.getImageUrl( "/bim/bam/bom/77.tuv", 400 );
		assertEquals( "http://foo.bar.zod/boink/bim/bam/bom/77_400x.tuv", url);

		url = connection.getImageUrl( "/bim/bam/bom/77.tuv", 0, 600 );
		assertEquals( "http://foo.bar.zod/boink/bim/bam/bom/77_x600.tuv", url);

		url = connection.getImageUrl( "/bim/bam/bom/77.tuv", 400, 600 );
		assertEquals( "http://foo.bar.zod/boink/bim/bam/bom/77_400x600.tuv", url);

		url = connection.getImageUrl( "/bim/bam/bom/77.tuv", 400, 600, 1 );
		assertEquals( "http://foo.bar.zod/boink/bim/bam/bom/77_400x600_1.tuv", url);

		url = connection.getImageUrl( "/bim/bam/bom/77.tuv", 400, 600, 1, "xpt" );
		assertEquals( "http://foo.bar.zod/boink/bim/bam/bom/77_400x600_1.xpt", url);
	}
}
