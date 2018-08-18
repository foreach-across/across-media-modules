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

import com.foreach.across.modules.webcms.domain.domain.config.WebCmsMultiDomainConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsDomainMetadataFactory
{
	private WebCmsDomain one = WebCmsDomain.builder().id( 123L ).build();

	@Mock
	private WebCmsMultiDomainConfiguration multiDomainConfiguration;

	@Mock
	private AutowireCapableBeanFactory beanFactory;

	@InjectMocks
	private WebCmsDomainMetadataFactoryImpl metadataFactory;

	@Test
	public void noDomainReturnsNoMetadataIfNotConfigured() {
		assertNull( metadataFactory.createMetadataForDomain( WebCmsDomain.NONE ) );
	}

	@Test
	public void noDomainReturnsMetadataBeanByNameIfBeanNameConfigured() {
		when( multiDomainConfiguration.getNoDomainMetadataBeanName() ).thenReturn( "noDomainMetadataBean" );
		when( beanFactory.getBean( "noDomainMetadataBean" ) ).thenReturn( "hello no-domain" );
		assertEquals( "hello no-domain", metadataFactory.createMetadataForDomain( WebCmsDomain.NONE ) );
	}

	@Test
	public void noMetadataReturnedForDomainIfNoClassNameConfigured() {
		when( multiDomainConfiguration.getMetadataClass() ).thenReturn( null );
		assertNull( metadataFactory.createMetadataForDomain( one ) );
		verifyNoMoreInteractions( beanFactory );
	}

	@Test
	public void metadataGetsCreatedByThenBeanFactory() {
		doReturn( WebCmsDomainAware.class ).when( multiDomainConfiguration ).getMetadataClass();
		WebCmsDomainAware metadata = mock( WebCmsDomainAware.class );
		when( beanFactory.createBean( WebCmsDomainAware.class ) ).thenReturn( metadata );

		assertSame( metadata, metadataFactory.createMetadataForDomain( one ) );
		verify( metadata ).setWebCmsDomain( one );
	}
}
