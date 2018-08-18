/*
 * Copyright 2017 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.modules.webcms.domain.image;

import com.foreach.across.modules.webcms.domain.WebCmsObjectNotFoundException;
import com.foreach.across.modules.webcms.domain.image.connector.WebCmsImageConnector;
import com.foreach.across.modules.webcms.infrastructure.WebCmsUtils;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.DigestUtils;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
@RunWith(MockitoJUnitRunner.class)
public class TestStringToWebCmsImageConverter
{
	private final WebCmsImage image = new WebCmsImage();

	@Mock
	private ApplicationContext applicationContext;

	@Mock
	private WebCmsImageConnector imageConnector;

	@Mock
	private WebCmsImageRepository imageRepository;

	@InjectMocks
	private StringToWebCmsImageConverter converter;

	@Test(expected = WebCmsObjectNotFoundException.class)
	public void conversionExceptionIsThrownIfImageNotFound() {
		converter.convert( WebCmsUtils.generateObjectId( WebCmsImage.COLLECTION_ID ) );
	}

	@Test
	public void objectIdIsLookedUpImmediately() {
		String objectId = WebCmsUtils.generateObjectId( WebCmsImage.COLLECTION_ID );
		when( imageRepository.findOneByObjectId( objectId ) ).thenReturn( image );
		assertSame( image, converter.convert( objectId ) );
	}

	@Test
	public void objectIdIsGeneratedAndImageFoundReturned() {
		String path = "classpath:/test.resource";
		String objectId = WebCmsUtils.prefixObjectIdForCollection( "import-" + DigestUtils.md5DigestAsHex( path.getBytes() ), WebCmsImage.COLLECTION_ID );
		when( imageRepository.findOneByObjectId( objectId ) ).thenReturn( image );

		assertSame( image, converter.convert( path ) );
		assertSame( image, converter.convert( objectId ) );
	}

	@Test(expected = WebCmsObjectNotFoundException.class)
	public void ifResourceDoesNotExistExceptionIsThrown() {
		when( applicationContext.getResource( "classpath:/dont/exist" ) ).thenReturn( mock( Resource.class ) );
		converter.convert( "classpath:/dont/exist" );
	}

	@SneakyThrows
	@Test
	public void imageIsCreatedAndResourceUploadedIfFound() {
		String path = "classpath:/test.resource";
		String expectedObjectId = WebCmsUtils.prefixObjectIdForCollection(
				"import-" + DigestUtils.md5DigestAsHex( path.getBytes() ), WebCmsImage.COLLECTION_ID
		);

		Resource resource = mock( Resource.class );
		when( applicationContext.getResource( path ) ).thenReturn( resource );
		when( resource.exists() ).thenReturn( true );
		when( resource.getFilename() ).thenReturn( "my-image.jpeg" );
		byte[] data = new byte[] { 12, 45 };
		when( resource.getInputStream() ).thenReturn( new ByteArrayInputStream( data ) );

		WebCmsImage image = converter.convert( path );
		assertNotNull( image );
		assertEquals( expectedObjectId, image.getObjectId() );
		assertEquals( "my-image.jpeg", image.getName() );
		assertTrue( image.isPublished() );

		InOrder ordered = Mockito.inOrder( imageRepository, imageConnector );

		ordered.verify( imageConnector ).saveImageData( image, data );
		ordered.verify( imageRepository ).save( image );
	}
}
