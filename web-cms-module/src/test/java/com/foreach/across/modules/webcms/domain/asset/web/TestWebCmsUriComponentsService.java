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

import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import com.foreach.across.modules.webcms.domain.domain.web.WebCmsDomainUrlConfigurer;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsUriComponentsServiceImpl;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsUriComponentsService
{
	private WebCmsDomain domain = new WebCmsDomain();

	@Mock
	private WebCmsDomainUrlConfigurer urlConfigurer;

	@Mock
	private WebCmsMultiDomainService multiDomainService;

	@Mock
	private WebCmsAssetEndpointRepository assetEndpointRepository;

	@InjectMocks
	@Spy
	private WebCmsUriComponentsServiceImpl uriComponentsService;

	@Test
	public void buildUriComponentsAssetDomainBoundNoDomain() {
		multiDomainService.attachDomainContext( WebCmsDomain.NONE );
		WebCmsAsset asset = new WebCmsArticle();

		when( multiDomainService.isDomainBound( eq( asset ) ) ).thenReturn( true );
		when( multiDomainService.getCurrentDomain() ).thenReturn( domain );
		when( assetEndpointRepository.findOneByAssetAndDomain( asset, WebCmsDomain.NONE ) ).thenReturn( new WebCmsAssetEndpoint() );

		Optional<UriComponentsBuilder> uriComponents = uriComponentsService.buildUriComponents( asset );

		verify( multiDomainService, times( 1 ) ).isDomainBound( asset );
		verify( multiDomainService, times( 1 ) ).getCurrentDomain();
		verify( uriComponentsService, times( 1 ) ).buildUriComponents( asset, domain );

		assertEquals( Optional.empty(), uriComponents );
	}

	@Test
	public void buildUriComponentsAssetDomainBoundDomain() {
		multiDomainService.attachDomainContext( WebCmsDomain.NONE );
		WebCmsAsset asset = new WebCmsArticle();
		asset.setDomain( domain );

		when( multiDomainService.isDomainBound( eq( asset ) ) ).thenReturn( true );
		when( assetEndpointRepository.findOneByAssetAndDomain( asset, WebCmsDomain.NONE ) ).thenReturn( new WebCmsAssetEndpoint() );

		Optional<UriComponentsBuilder> uriComponents = uriComponentsService.buildUriComponents( asset );

		verify( multiDomainService, times( 1 ) ).isDomainBound( asset );
		verify( uriComponentsService, times( 1 ) ).buildUriComponents( asset, domain );

		assertEquals( Optional.empty(), uriComponents );
	}

	@Test
	public void buildUriComponentsAssetNotDomainBound() {
		multiDomainService.attachDomainContext( WebCmsDomain.NONE );
		WebCmsAsset asset = new WebCmsArticle();

		when( multiDomainService.isDomainBound( eq( asset ) ) ).thenReturn( false );
		when( assetEndpointRepository.findOneByAssetAndDomain( asset, WebCmsDomain.NONE ) ).thenReturn( new WebCmsAssetEndpoint() );

		Optional<UriComponentsBuilder> uriComponents = uriComponentsService.buildUriComponents( asset );

		verify( multiDomainService, times( 1 ) ).isDomainBound( asset );
		verify( uriComponentsService, times( 1 ) ).buildUriComponents( asset, WebCmsDomain.NONE );

		assertEquals( Optional.empty(), uriComponents );
	}

	@Test
	public void buildUriComponentsAssetDomain() {
		WebCmsAsset asset = new WebCmsArticle();
		WebCmsAssetEndpoint endpoint = new WebCmsAssetEndpoint();
		asset.setDomain( domain );

		when( assetEndpointRepository.findOneByAssetAndDomain( asset, domain ) ).thenReturn( endpoint );

		Optional<UriComponentsBuilder> uriComponents = uriComponentsService.buildUriComponents( asset, domain );

		verify( assetEndpointRepository, times( 1 ) ).findOneByAssetAndDomain( asset, domain );
		verify( uriComponentsService, times( 1 ) ).buildUriComponents( endpoint, domain );

		assertEquals( Optional.empty(), uriComponents );
	}

	@Test
	public void buildUriComponentsEndpoint() {
		multiDomainService.attachDomainContext( WebCmsDomain.NONE );
		WebCmsAssetEndpoint assetEndpoint = new WebCmsAssetEndpoint();
		assetEndpoint.setUrls( Collections.singletonList( new WebCmsUrl().toBuilder().primary( true ).path( "/url-path" ).build() ) );
		assetEndpoint.setDomain( domain );

		when( multiDomainService.isDomainBound( eq( assetEndpoint ) ) ).thenReturn( true );

		uriComponentsService.buildUriComponents( assetEndpoint );

		verify( uriComponentsService, times( 1 ) ).buildUriComponents( assetEndpoint, domain );
	}

	@Test
	public void buildUriComponentsEndPointDomain() {
		WebCmsAssetEndpoint assetEndpoint = new WebCmsAssetEndpoint();
		WebCmsUrl primary = new WebCmsUrl().toBuilder().primary( true ).path( "/url-path" ).build();
		assetEndpoint.setUrls( Collections.singletonList( primary ) );
		assetEndpoint.setDomain( domain );

		uriComponentsService.buildUriComponents( assetEndpoint, domain );

		verify( uriComponentsService, times( 1 ) ).buildUriComponents( primary, domain );
	}

	@Test
	public void buildUriComponentsUrl() {
		WebCmsAssetEndpoint assetEndpoint = new WebCmsAssetEndpoint();
		WebCmsUrl url = new WebCmsUrl().toBuilder().primary( true ).path( "/url-path" ).build();
		assetEndpoint.setUrls( Collections.singletonList( url ) );
		assetEndpoint.setDomain( domain );
		url.setEndpoint( assetEndpoint );

		when( multiDomainService.isDomainBound( eq( assetEndpoint ) ) ).thenReturn( true );

		uriComponentsService.buildUriComponents( url );

		verify( uriComponentsService, times( 1 ) ).buildUriComponents( url, domain );
	}

	@Test
	public void buildUriComponentsUrlDomain() {
		WebCmsAssetEndpoint assetEndpoint = new WebCmsAssetEndpoint();
		WebCmsUrl url = new WebCmsUrl().toBuilder().primary( true ).path( "/url-path" ).build();
		assetEndpoint.setUrls( Collections.singletonList( url ) );
		assetEndpoint.setDomain( domain );
		url.setEndpoint( assetEndpoint );

		when( multiDomainService.isDomainBound( eq( assetEndpoint ) ) ).thenReturn( true );
		when( multiDomainService.getMetadataForDomain( eq( domain ), any() ) ).thenReturn( urlConfigurer );
		when( urlConfigurer.isAlwaysPrefix() ).thenReturn( false );

		Optional<UriComponentsBuilder> uriComponents = uriComponentsService.buildUriComponents( url, domain );

		assertEquals( uriComponents.get().toUriString(), "/url-path" );
	}

	@Test
	public void buildUriComponentsUrlDomainAlwaysPrefixDoubleSlash() {
		WebCmsAssetEndpoint assetEndpoint = new WebCmsAssetEndpoint();
		WebCmsUrl url = new WebCmsUrl().toBuilder().primary( true ).path( "/url-path" ).build();
		assetEndpoint.setUrls( Collections.singletonList( url ) );
		assetEndpoint.setDomain( domain );
		url.setEndpoint( assetEndpoint );

		when( multiDomainService.isDomainBound( eq( assetEndpoint ) ) ).thenReturn( true );
		when( multiDomainService.getMetadataForDomain( eq( domain ), any() ) ).thenReturn( urlConfigurer );
		when( multiDomainService.isCurrentDomain( domain ) ).thenReturn( true );
		when( urlConfigurer.isAlwaysPrefix() ).thenReturn( true );
		when( urlConfigurer.getUrlPrefix() ).thenReturn( "https://my-domain.be/" );

		Optional<UriComponentsBuilder> uriComponents = uriComponentsService.buildUriComponents( url, domain );

		assertEquals( uriComponents.get().toUriString(), "https://my-domain.be/url-path" );
	}

	@Test
	public void buildUriComponentsUrlDomainAlwaysPrefixNoSlash() {
		WebCmsAssetEndpoint assetEndpoint = new WebCmsAssetEndpoint();
		WebCmsUrl url = new WebCmsUrl().toBuilder().primary( true ).path( "url-path" ).build();
		assetEndpoint.setUrls( Collections.singletonList( url ) );
		assetEndpoint.setDomain( domain );
		url.setEndpoint( assetEndpoint );

		when( multiDomainService.isDomainBound( eq( assetEndpoint ) ) ).thenReturn( true );
		when( multiDomainService.getMetadataForDomain( eq( domain ), any() ) ).thenReturn( urlConfigurer );
		when( multiDomainService.isCurrentDomain( domain ) ).thenReturn( true );
		when( urlConfigurer.isAlwaysPrefix() ).thenReturn( true );
		when( urlConfigurer.getUrlPrefix() ).thenReturn( "https://my-domain.be" );

		Optional<UriComponentsBuilder> uriComponents = uriComponentsService.buildUriComponents( url, domain );

		assertEquals( uriComponents.get().toUriString(), "https://my-domain.be/url-path" );
	}

	@Test
	public void buildUriComponentsUrlDomainAlwaysPrefixSlashPrefix() {
		WebCmsAssetEndpoint assetEndpoint = new WebCmsAssetEndpoint();
		WebCmsUrl url = new WebCmsUrl().toBuilder().primary( true ).path( "url-path" ).build();
		assetEndpoint.setUrls( Collections.singletonList( url ) );
		assetEndpoint.setDomain( domain );
		url.setEndpoint( assetEndpoint );

		when( multiDomainService.isDomainBound( eq( assetEndpoint ) ) ).thenReturn( true );
		when( multiDomainService.getMetadataForDomain( eq( domain ), any() ) ).thenReturn( urlConfigurer );
		when( multiDomainService.isCurrentDomain( domain ) ).thenReturn( true );
		when( urlConfigurer.isAlwaysPrefix() ).thenReturn( true );
		when( urlConfigurer.getUrlPrefix() ).thenReturn( "https://my-domain.be/" );

		Optional<UriComponentsBuilder> uriComponents = uriComponentsService.buildUriComponents( url, domain );

		assertEquals( uriComponents.get().toUriString(), "https://my-domain.be/url-path" );
	}

	@Test
	public void buildUriComponentsUrlDomainAlwaysPrefixSlashPath() {
		WebCmsAssetEndpoint assetEndpoint = new WebCmsAssetEndpoint();
		WebCmsUrl url = new WebCmsUrl().toBuilder().primary( true ).path( "/url-path" ).build();
		assetEndpoint.setUrls( Collections.singletonList( url ) );
		assetEndpoint.setDomain( domain );
		url.setEndpoint( assetEndpoint );

		when( multiDomainService.isDomainBound( eq( assetEndpoint ) ) ).thenReturn( true );
		when( multiDomainService.getMetadataForDomain( eq( domain ), any() ) ).thenReturn( urlConfigurer );
		when( multiDomainService.isCurrentDomain( domain ) ).thenReturn( true );
		when( urlConfigurer.isAlwaysPrefix() ).thenReturn( true );
		when( urlConfigurer.getUrlPrefix() ).thenReturn( "https://my-domain.be" );

		Optional<UriComponentsBuilder> uriComponents = uriComponentsService.buildUriComponents( url, domain );

		assertEquals( uriComponents.get().toUriString(), "https://my-domain.be/url-path" );
	}

}
