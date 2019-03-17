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

package it.multidomainreference;

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainRepository;
import com.foreach.across.modules.webcms.domain.redirect.WebCmsRemoteEndpoint;
import com.foreach.across.modules.webcms.domain.redirect.WebCmsRemoteEndpointRepository;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import it.AbstractMultiDomainCmsApplicationWithTestDataIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class ITMultiDomainRedirectReferenceData extends AbstractMultiDomainCmsApplicationWithTestDataIT
{
	@Autowired
	private WebCmsRemoteEndpointRepository remoteEndpointRepository;

	@Autowired
	private WebCmsDomainRepository domainRepository;

	@Test
	@Transactional
	void googleRedirectShouldHaveBeenImported() {
		WebCmsRemoteEndpoint endpoint = remoteEndpointRepository.findOneByTargetUrlAndDomain( "https://google.com", WebCmsDomain.NONE );
		assertNotNull( endpoint );
		Collection<WebCmsUrl> urls = endpoint.getUrls();
		assertEquals( 2, urls.size() );
		WebCmsUrl testUrl = endpoint.getUrlWithPath( "/test-url" ).orElse( null );
		assertNotNull( testUrl );
		assertEquals( HttpStatus.valueOf( 301 ), testUrl.getHttpStatus() );
		WebCmsUrl google = endpoint.getUrlWithPath( "/google" ).orElse( null );
		assertNotNull( google );
		assertEquals( HttpStatus.valueOf( 302 ), google.getHttpStatus() );
	}

	@Test
	@Transactional
	void googleBeRedirectShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "be-foreach" );
		WebCmsRemoteEndpoint endpoint = remoteEndpointRepository.findOneByTargetUrlAndDomain( "https://google.be", domain );
		assertNotNull( endpoint );
		Collection<WebCmsUrl> urls = endpoint.getUrls();
		assertEquals( 2, urls.size() );
		WebCmsUrl testUrl = endpoint.getUrlWithPath( "/be-test-url" ).orElse( null );
		assertNotNull( testUrl );
		assertEquals( HttpStatus.valueOf( 301 ), testUrl.getHttpStatus() );
		WebCmsUrl google = endpoint.getUrlWithPath( "/be-google" ).orElse( null );
		assertNotNull( google );
		assertEquals( HttpStatus.valueOf( 302 ), google.getHttpStatus() );
	}

	@Test
	@Transactional
	void outlookRedirectShouldHaveBeenImportedAndExtended() {
		WebCmsRemoteEndpoint endpoint = remoteEndpointRepository.findOneByTargetUrlAndDomain( "http://outlook.com", WebCmsDomain.NONE );
		assertNotNull( endpoint );
		Collection<WebCmsUrl> urls = endpoint.getUrls();
		assertEquals( 1, urls.size() );
		WebCmsUrl outlook = endpoint.getUrlWithPath( "/outlook" ).orElse( null );
		assertNotNull( outlook );
		assertEquals( HttpStatus.valueOf( 301 ), outlook.getHttpStatus() );
		WebCmsUrl deleted = endpoint.getUrlWithPath( "/to-be-deleted" ).orElse( null );
		assertNull( deleted );
	}

	@Test
	@Transactional
	void outlookBeRedirectShouldHaveBeenImportedAndExtended() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "be-foreach" );
		WebCmsRemoteEndpoint endpoint = remoteEndpointRepository.findOneByTargetUrlAndDomain( "http://outlook.be", domain );
		assertNotNull( endpoint );
		Collection<WebCmsUrl> urls = endpoint.getUrls();
		assertEquals( 1, urls.size() );
		WebCmsUrl outlook = endpoint.getUrlWithPath( "/be-outlook" ).orElse( null );
		assertNotNull( outlook );
		assertEquals( HttpStatus.valueOf( 301 ), outlook.getHttpStatus() );
		WebCmsUrl deleted = endpoint.getUrlWithPath( "/to-be-deleted" ).orElse( null );
		assertNull( deleted );
	}
}
