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

package it.assets;

import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticleRepository;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticleType;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetService;
import com.foreach.across.modules.webcms.domain.domain.CloseableWebCmsDomainContext;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainRepository;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpointService;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsUriComponentsService;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublicationRepository;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeRegistry;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierRepository;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.foreach.across.modules.webcms.domain.url.repositories.WebCmsUrlRepository;
import it.AbstractMultiDomainCmsApplicationWithTestDataIT;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ITWebCmsAssetService extends AbstractMultiDomainCmsApplicationWithTestDataIT
{
	@Autowired
	private WebCmsEndpointService endpointService;

	@Autowired
	private WebCmsUriComponentsService uriComponentsService;

	@Autowired
	private WebCmsAssetService assetService;

	@Autowired
	private WebCmsMultiDomainService multiDomainService;

	@Autowired
	private WebCmsDomainRepository domainRepository;

	@Autowired
	private WebCmsTypeSpecifierRepository typeSpecifierRepository;

	@Autowired
	private WebCmsPublicationRepository publicationRepository;

	@Autowired
	private WebCmsTypeRegistry typeRegistry;

	@Autowired
	private WebCmsArticleRepository articleRepository;

	@Autowired
	private WebCmsAssetEndpointRepository endpointRepository;

	@Autowired
	private WebCmsUrlRepository urlRepository;

	private WebCmsDomain domain;

	@Before
	public void setUp() {
		domain = WebCmsDomain.builder()
		                     .name( "My domain" )
		                     .domainKey( "my-domain" )
		                     .objectId( "wcm:domain:my-domain" )
		                     .attribute( "urlPrefix", "https://my-domain.be" )
		                     .attribute( "alwaysPrefix", String.valueOf( true ) )
		                     .build();
		domainRepository.save( domain );
	}

	@Test
	public void buildPreviewUrlForAsset() {
		WebCmsArticle asset = WebCmsArticle.builder()
		                                   .publication( publicationRepository.findOneByPublicationKeyAndDomain( "blogs", WebCmsDomain.NONE ) )
		                                   .articleType( (WebCmsArticleType) typeSpecifierRepository
				                                   .findOneByObjectTypeAndTypeKeyAndDomain(
						                                   typeRegistry.retrieveObjectType( WebCmsArticleType.class ).orElse( null ), "blog",
						                                   WebCmsDomain.NONE ) )
		                                   .title( "My asset" )
		                                   .domain( domain )
		                                   .build();
		articleRepository.save( asset );
		WebCmsAssetEndpoint endpoint = endpointRepository.findOneByAssetAndDomain( asset, asset.getDomain() );
		WebCmsUrl url = WebCmsUrl.builder().endpoint( endpoint ).primary( true ).httpStatus( HttpStatus.OK ).path( "my-path" ).build();
		urlRepository.save( url );

		try (CloseableWebCmsDomainContext ignore = multiDomainService.attachDomainContext( domain )) {
			Optional<UriComponentsBuilder> uriComponents = uriComponentsService.buildUriComponents( asset );
			Optional<String> previewUrl = assetService.buildPreviewUrl( asset );

			assertNotEquals( Optional.empty(), uriComponents );
			assertNotEquals( Optional.empty(), previewUrl );

			assertEquals( endpointService.appendPreviewCode( endpoint, uriComponents.get() ).toUriString(), previewUrl.get() );
		}

		articleRepository.delete( asset );
	}

	@Test
	public void buildPreviewUrlForAssetAndDomain() {
		WebCmsArticle assetNullDomain = WebCmsArticle.builder()
		                                             .publication( publicationRepository.findOneByPublicationKeyAndDomain( "blogs", WebCmsDomain.NONE ) )
		                                             .articleType( (WebCmsArticleType) typeSpecifierRepository
				                                             .findOneByObjectTypeAndTypeKeyAndDomain(
						                                             typeRegistry.retrieveObjectType( WebCmsArticleType.class ).orElse( null ), "blog",
						                                             WebCmsDomain.NONE ) )
		                                             .title( "My other asset" )
		                                             .domain( WebCmsDomain.NONE )
		                                             .build();
		articleRepository.save( assetNullDomain );
		WebCmsAssetEndpoint endpointNullDomain = endpointRepository.findOneByAssetAndDomain( assetNullDomain, assetNullDomain.getDomain() );
		WebCmsUrl urlNullDomain = WebCmsUrl.builder().endpoint( endpointNullDomain ).primary( true ).httpStatus( HttpStatus.OK ).path( "my-path" ).build();
		urlRepository.save( urlNullDomain );

		try (CloseableWebCmsDomainContext ignore = multiDomainService.attachDomainContext( domain )) {
			Optional<UriComponentsBuilder> uriComponents = uriComponentsService.buildUriComponents( assetNullDomain, WebCmsDomain.NONE );
			Optional<String> previewUrl = assetService.buildPreviewUrlOnDomain( assetNullDomain, WebCmsDomain.NONE );

			assertNotEquals( Optional.empty(), uriComponents );
			assertNotEquals( Optional.empty(), previewUrl );

			assertEquals( endpointService.appendPreviewCode( endpointNullDomain, uriComponents.get() ).toUriString(), previewUrl.get() );
		}

		articleRepository.delete( assetNullDomain );
	}

	@After
	public void reset() {
		domainRepository.delete( domain );
	}
}
