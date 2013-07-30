package com.foreach.imageserver.services.repositories;

import com.foreach.imageserver.business.ImageType;
import org.h2.util.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestDioContentLookupRepository
{
	private ImageLookupRepository imageLookupRepository;

	@Before
	public void before() {
		imageLookupRepository = new DioContentLookupRepository();
	}

	@Test
	public void getKnownImage() throws IOException {
		RepositoryLookupResult lookupResult = imageLookupRepository.fetchImage( "dc:2174372" );

		assertNotNull( lookupResult );
		assertEquals( ImageType.JPEG, lookupResult.getImageType() );
		IOUtils.copy( lookupResult.getContent(), new FileOutputStream( "c:/temp/diocontent.jpeg" ) );
	}

	/*
	private void getValidImage( ImageTestData imageTestData ) throws Exception {
		RepositoryLookupResult lookupResult = imageLookupRepository.fetchImage( webServer.imageUrl( imageTestData ) );

		assertEquals( RepositoryLookupStatus.SUCCESS, lookupResult.getStatus() );
		assertEquals( imageTestData.getImageType(), lookupResult.getImageType() );
		assertNotNull( lookupResult.getContent() );
		assertTrue( IOUtils.contentEquals( getClass().getResourceAsStream( imageTestData.getResourcePath() ),
		                                   lookupResult.getContent() ) );
	}*/

}
