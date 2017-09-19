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

package com.foreach.across.modules.webcms.domain.asset.web;

import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetServiceImpl;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import com.foreach.across.modules.webcms.domain.domain.web.WebCmsDomainUrlConfigurer;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpointService;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpointServiceImpl;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsUriComponentsServiceImpl;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrlCache;
import com.foreach.across.modules.webcms.domain.url.repositories.WebCmsUrlRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsAssetService
{
	private WebCmsDomain domain = new WebCmsDomain();

	@Mock
	private WebCmsDomainUrlConfigurer urlConfigurer;

	@Mock
	private WebCmsMultiDomainService multiDomainService;

	@Mock
	private WebCmsAssetEndpointRepository assetEndpointRepository;

	private WebCmsEndpointService endpointService;

	@Mock
	private WebCmsUrlRepository urlRepository;

	@Mock
	private WebCmsUrlCache urlCache;

	@Mock
	private AcrossEventPublisher acrossEventPublisher;

	@InjectMocks
	@Spy
	private WebCmsUriComponentsServiceImpl uriComponentsService;

	private WebCmsAssetServiceImpl assetService;

	@Before
	public void setUp() {
		endpointService = new WebCmsEndpointServiceImpl( assetEndpointRepository, urlRepository, urlCache, acrossEventPublisher, multiDomainService );
		assetService = new WebCmsAssetServiceImpl( uriComponentsService, endpointService, assetEndpointRepository, multiDomainService );
	}

	@Test
	public void buildPreviewUrlAssetDomainEqCurrentDomain() {
		WebCmsAsset asset = new WebCmsArticle();
		asset.setDomain( domain );

		WebCmsAssetEndpoint assetEndpoint = new WebCmsAssetEndpoint();
		assetEndpoint.setDomain( domain );
		assetEndpoint.setUrls( Collections.singletonList( new WebCmsUrl().toBuilder().primary( true ).path( "/url-path" ).build() ) );
		assetEndpoint.setId( 123L );

		when( multiDomainService.isDomainBound( eq( asset ) ) ).thenReturn( true );
		when( multiDomainService.getCurrentDomain() ).thenReturn( domain );
		when( multiDomainService.getMetadataForDomain( eq( domain ), any() ) ).thenReturn( urlConfigurer );
		when( urlConfigurer.isAlwaysPrefix() ).thenReturn( true );
		when( urlConfigurer.getUrlPrefix() ).thenReturn( "https://my-domain.be/" );
		when( multiDomainService.isCurrentDomain( domain ) ).thenReturn( true );
		when( assetEndpointRepository.findOneByAssetAndDomain( asset, domain ) ).thenReturn( assetEndpoint );
		Optional<UriComponentsBuilder> uriComponents = uriComponentsService.buildUriComponents( asset );

		when( multiDomainService.getCurrentDomainForEntity( asset ) ).thenReturn( domain );
		Optional<String> uriComponentsPreview = assetService.buildPreviewUrl( asset );

		assertNotEquals( Optional.empty(), uriComponents );
		assertNotEquals( Optional.empty(), uriComponentsPreview );
		assertEquals( endpointService.appendPreviewCode( assetEndpoint, uriComponents.get() ).toUriString(), uriComponentsPreview.get() );
	}

	@Test
	public void buildPreviewUrlAssetDomainNotEqCurrentDomain() {
		WebCmsAsset asset = new WebCmsArticle();
		asset.setDomain( WebCmsDomain.NONE );

		WebCmsAssetEndpoint assetEndpoint = new WebCmsAssetEndpoint();
		assetEndpoint.setDomain( domain );
		assetEndpoint.setUrls( Collections.singletonList( new WebCmsUrl().toBuilder().primary( true ).path( "/url-path" ).build() ) );
		assetEndpoint.setId( 123L );

		when( multiDomainService.isDomainBound( eq( asset ) ) ).thenReturn( true );
		when( multiDomainService.getCurrentDomain() ).thenReturn( domain );
		when( multiDomainService.isCurrentDomain( WebCmsDomain.NONE ) ).thenReturn( false );
		when( assetEndpointRepository.findOneByAssetAndDomain( asset, domain ) ).thenReturn( assetEndpoint );
		when( multiDomainService.getMetadataForDomain( eq( domain ), any() ) ).thenReturn( urlConfigurer );
		when( urlConfigurer.isAlwaysPrefix() ).thenReturn( true );
		when( urlConfigurer.getUrlPrefix() ).thenReturn( "https://my-domain.be/" );
		Optional<UriComponentsBuilder> uriComponents = uriComponentsService.buildUriComponents( asset );

		when( multiDomainService.getCurrentDomainForEntity( asset ) ).thenReturn( domain );
		Optional<String> uriComponentsPreview = assetService.buildPreviewUrl( asset );
		assertNotEquals( Optional.empty(), uriComponents );
		assertNotEquals( Optional.empty(), uriComponentsPreview );
		assertEquals( endpointService.appendPreviewCode( assetEndpoint, uriComponents.get() ).toUriString(), uriComponentsPreview.get() );
	}
}
