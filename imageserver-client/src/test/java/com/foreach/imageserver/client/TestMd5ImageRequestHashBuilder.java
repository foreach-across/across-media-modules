package com.foreach.imageserver.client;

import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.dto.ImageTypeDto;
import com.foreach.imageserver.dto.ImageVariantDto;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
public class TestMd5ImageRequestHashBuilder
{
	private Md5ImageRequestHashBuilder hashBuilder;

	@Before
	public void before() {
		hashBuilder = new Md5ImageRequestHashBuilder( "test" );
	}

	@Test(expected = IllegalArgumentException.class)
	public void nonNullTokenIsRequired() {
		hashBuilder = new Md5ImageRequestHashBuilder( null );
	}

	@Test
	public void blankTokenGeneratesValidHash() {
		hashBuilder = new Md5ImageRequestHashBuilder( "" );

		String hash = hashBuilder.calculateHash( "ONLINE", null, new ImageResolutionDto( 1000, 2000 ), null );
		assertNotNull( hash );
		assertEquals( "efb552c12c1d878a9ff603d3e0dc283a".length(), hash.length() );
	}

	@Test
	public void sameParametersResultInSameHash() {
		for ( int i = 0; i < 50; i++ ) {
			assertEquals(
					"efb552c12c1d878a9ff603d3e0dc283a",
					hashBuilder.calculateHash( "ONLINE", null, new ImageResolutionDto( 1000, 2000 ), null )
			);
		}
	}

	@Test
	public void validHashExamples() {
		String hash = hashBuilder.calculateHash( "ONLINE",
		                                         null,
		                                         new ImageResolutionDto( 1000, 2000 ),
		                                         new ImageVariantDto( ImageTypeDto.TIFF ) );
		assertEquals( "e71131abb6817f79b17e8e39078b4255", hash );

		hash = hashBuilder.calculateHash( "DIGITAL",
		                                  null,
		                                  new ImageResolutionDto( 0, 2000 ),
		                                  new ImageVariantDto( ImageTypeDto.TIFF ) );
		assertEquals( "3ef93ef809cfd041ba4d99061bd52471", hash );

		hash = hashBuilder.calculateHash( "SITE",
		                                  null,
		                                  new ImageResolutionDto( 1000, 0 ),
		                                  new ImageVariantDto( ImageTypeDto.TIFF ) );
		assertEquals( "61f249032da4eec3a1d5ffb32ddf5911", hash );

		ImageVariantDto variant = new ImageVariantDto( ImageTypeDto.JPEG );
		variant.setBoundaries( new DimensionsDto( 100, 1000 ) );

		hash = hashBuilder.calculateHash( "TABLET",
		                                  "3/2",
		                                  new ImageResolutionDto( 200, 0 ),
		                                  variant );
		assertEquals( "b84f0a6a94945daa7d2f9d4411aad0c9", hash );

		hash = hashBuilder.calculateHash( "ONLINE", null, new ImageResolutionDto( 1000, 2000 ), null );
		assertEquals( "efb552c12c1d878a9ff603d3e0dc283a", hash );
	}

	@Test
	public void otherTokenResultsInDifferentHash() {
		Md5ImageRequestHashBuilder otherHashBuilder = new Md5ImageRequestHashBuilder( "other" );

		String hash = otherHashBuilder.calculateHash( "ONLINE", null, new ImageResolutionDto( 1000, 2000 ), null );
		assertNotNull( hash );
		assertEquals( "efb552c12c1d878a9ff603d3e0dc283a".length(), hash.length() );
		assertNotEquals(
				hashBuilder.calculateHash( "ONLINE", null, new ImageResolutionDto( 1000, 2000 ), null ),
				hash
		);
	}

	@Test
	public void defaultMethodOnInterface() {
		ImageRequestHashBuilder otherHashBuilder = ImageRequestHashBuilder.md5( "test" );

		assertEquals(
				hashBuilder.calculateHash( "ONLINE", null, new ImageResolutionDto( 1000, 2000 ), null ),
				otherHashBuilder.calculateHash( "ONLINE", null, new ImageResolutionDto( 1000, 2000 ), null )
		);
	}
}
