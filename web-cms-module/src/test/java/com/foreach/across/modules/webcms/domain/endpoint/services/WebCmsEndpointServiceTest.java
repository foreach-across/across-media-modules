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

package com.foreach.across.modules.webcms.domain.endpoint.services;

import com.foreach.across.modules.webcms.config.WebCmsModuleCache;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.foreach.across.modules.webcms.domain.url.repositories.WebCmsUrlRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class WebCmsEndpointServiceTest
{
	private WebCmsEndpointServiceImpl webCmsEndpointService;

	@Mock
	private WebCmsUrlRepository repository;

	@Mock
	private CacheManager cachemanager;

	@Mock
	private WebCmsEndpoint endpoint;

	@Before
	public void setUp() throws Exception {
		webCmsEndpointService = new WebCmsEndpointServiceImpl( repository, cachemanager );
		when( cachemanager.getCache( WebCmsModuleCache.PATH_TO_URL_CACHE ) ).thenReturn( new ConcurrentMapCache( WebCmsModuleCache.PATH_TO_URL_CACHE ) );
		webCmsEndpointService.loadActualCache();
	}

	@Test
	public void fetchFromCache() {
		WebCmsUrl url = WebCmsUrl.builder()
		                         .id( 646L )
		                         .path( "/path" )
		                         .primary( false )
		                         .httpStatus( HttpStatus.MOVED_PERMANENTLY )
		                         .endpoint( endpoint )
		                         .build();

		when( repository.findOne( 646L ) ).thenReturn( url );
		when( repository.findByPath( "/path" ) ).thenReturn( Arrays.asList( url ) );

		assertTrue( webCmsEndpointService.getUrlForPath( "/path" ).isPresent() );
		assertTrue( webCmsEndpointService.getUrlForPath( "/path" ).isPresent() );
		assertTrue( webCmsEndpointService.getUrlForPath( "/path" ).isPresent() );
		assertTrue( webCmsEndpointService.getUrlForPath( "/path" ).isPresent() );

		verify( repository, times( 1 ) ).findByPath( anyString() );
		verify( repository, times( 3 ) ).findOne( anyLong() );
	}

}