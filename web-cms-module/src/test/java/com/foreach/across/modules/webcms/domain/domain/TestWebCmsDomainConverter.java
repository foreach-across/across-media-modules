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

package com.foreach.across.modules.webcms.domain.domain;

import com.foreach.across.modules.webcms.infrastructure.WebCmsUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsDomainConverter
{
	private final WebCmsDomain webCmsDomain = new WebCmsDomain();

	@Mock
	private WebCmsDomainRepository domainRepository;

	@InjectMocks
	private StringToWebCmsDomainConverter converter;

	@Test
	public void objectIdIsLookedUpImmediately() {
		String objectId = WebCmsUtils.generateObjectId( WebCmsDomain.COLLECTION_ID );
		when( domainRepository.findOneByObjectId( objectId ) ).thenReturn( webCmsDomain );
		assertSame( webCmsDomain, converter.convert( objectId ) );
		verify( domainRepository, never() ).findOne( anyLong() );
		verify( domainRepository, never() ).findOneByDomainKey( any() );
	}

	@Test
	public void idIsLookedUpImmediately() {
		Long id = 1L;
		when( domainRepository.findOne( id ) ).thenReturn( webCmsDomain );
		assertSame( webCmsDomain, converter.convert( String.valueOf( id ) ) );
		verify( domainRepository, never() ).findOneByObjectId( any() );
		verify( domainRepository, never() ).findOneByDomainKey( any() );
	}

	@Test
	public void domainKeyIsLookedUpImmediately() {
		String key = "my-domain";
		when( domainRepository.findOneByDomainKey( key ) ).thenReturn( webCmsDomain );
		assertSame( webCmsDomain, converter.convert( key ) );
		verify( domainRepository, never() ).findOneByObjectId( any() );
		verify( domainRepository, never() ).findOne( anyLong() );
	}

	@Test
	public void emptyString() {
		when( domainRepository.findOneByDomainKey( "" ) ).thenReturn( null );
		assertNull( converter.convert( "" ) );
		verify( domainRepository, never() ).findOneByObjectId( any() );
		verify( domainRepository, never() ).findOne( anyLong() );
		verify( domainRepository, never() ).findOneByDomainKey( any() );
	}
}
