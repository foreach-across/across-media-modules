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

package it;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ConfigurerScope;
import com.foreach.across.modules.spring.security.SpringSecurityModuleCache;
import com.foreach.across.modules.webcms.WebCmsModule;
import com.foreach.across.modules.webcms.config.WebCmsModuleCache;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.endpoint.services.WebCmsEndpointService;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageRepository;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.foreach.across.modules.webcms.domain.url.repositories.WebCmsUrlRepository;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.across.test.AcrossWebAppConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@AcrossWebAppConfiguration
@ContextConfiguration(classes = ITWebCmsModuleWithCaching.CacheConfig.class)
public class ITWebCmsModuleWithCaching
{
	@Autowired
	private WebCmsEndpointService webCmsEndpointService;

	@Autowired
	private WebCmsUrlRepository repository;

	@Autowired
	private WebCmsPageRepository pageRepository;

	@Autowired
	private WebCmsAssetEndpointRepository endpointRepository;

	private Map<Object, Object> pathToUrlCache;
	private WebCmsPage page;
	private WebCmsAssetEndpoint endpoint;

	@Autowired
	public void registerCaches(
			@Qualifier(WebCmsModuleCache.PATH_TO_URL_CACHE) ConcurrentMapCache pathToUrlCache
	) {
		this.pathToUrlCache = pathToUrlCache.getNativeCache();
	}

	@Before
	public void before() {
		pathToUrlCache.clear();

		page = WebCmsPage.builder()
		                 .id( 1000L )
		                 .pathSegment( "about" )
		                 .canonicalPath( "/a" )
		                 .title( "About page" )
		                 .published( true )
		                 .build();
		pageRepository.save( page );

		endpoint = endpointRepository.findOneByAsset( page );
	}

	@Test
	public void idOfMissingWebCmsUrlGetsCached() {
		assertTrue( pathToUrlCache.isEmpty() );

		Optional<WebCmsUrl> url = webCmsEndpointService.getUrlForPath( "/path" );

		assertFalse( url.isPresent() );
		assertTrue( pathToUrlCache.containsKey( "/path" ) );
		assertEquals( Optional.empty(), pathToUrlCache.get( "/path" ) );
	}

	@Test
	public void createUrlAndGetItFromCache() {
		assertTrue( pathToUrlCache.isEmpty() );

		repository.save( WebCmsUrl.builder()
		                          .id( 646L )
		                          .path( "/path" )
		                          .primary( false )
		                          .httpStatus( HttpStatus.MOVED_PERMANENTLY )
		                          .endpoint( endpoint ).build() );
		assertTrue( pathToUrlCache.isEmpty() );

		Optional<WebCmsUrl> url = webCmsEndpointService.getUrlForPath( "/path" );

		assertTrue( url.isPresent() );
		assertTrue( pathToUrlCache.containsKey( "/path" ) );
		assertEquals( Optional.of( 646L ), pathToUrlCache.get( "/path" ) );
	}

	@Test
	public void createAndDeleteUrlAndGetItFromCache() {
		assertTrue( pathToUrlCache.isEmpty() );

		WebCmsUrl webCmsUrl = WebCmsUrl.builder()
		                               .id( 646L )
		                               .path( "/path" )
		                               .primary( false )
		                               .httpStatus( HttpStatus.MOVED_PERMANENTLY )
		                               .endpoint( endpoint ).build();
		repository.save( webCmsUrl );
		assertTrue( pathToUrlCache.isEmpty() );

		Optional<WebCmsUrl> url = webCmsEndpointService.getUrlForPath( "/path" );

		assertTrue( url.isPresent() );
		assertTrue( pathToUrlCache.containsKey( "/path" ) );
		assertEquals( Optional.of( 646L ), pathToUrlCache.get( "/path" ) );

		repository.delete( webCmsUrl );
		assertTrue( pathToUrlCache.isEmpty() );

		url = webCmsEndpointService.getUrlForPath( "/path" );

		assertFalse( url.isPresent() );
	}

	@Test
	public void createAndUpdateUrlAndGetItFromCache() {
		assertTrue( pathToUrlCache.isEmpty() );

		WebCmsUrl webCmsUrl = WebCmsUrl.builder()
		                               .id( 646L )
		                               .path( "/path" )
		                               .primary( false )
		                               .httpStatus( HttpStatus.MOVED_PERMANENTLY )
		                               .endpoint( endpoint ).build();
		repository.save( webCmsUrl );
		assertTrue( pathToUrlCache.isEmpty() );

		Optional<WebCmsUrl> url = webCmsEndpointService.getUrlForPath( "/path" );

		assertTrue( url.isPresent() );
		assertTrue( pathToUrlCache.containsKey( "/path" ) );
		assertEquals( Optional.of( 646L ), pathToUrlCache.get( "/path" ) );

		webCmsUrl.setPath( "/path2" );
		repository.save( webCmsUrl );
		assertTrue( pathToUrlCache.isEmpty() );

		Optional<WebCmsUrl> oldPath = webCmsEndpointService.getUrlForPath( "/path" );
		assertFalse( oldPath.isPresent() );

		Optional<WebCmsUrl> newPath = webCmsEndpointService.getUrlForPath( "/path2" );
		assertTrue( newPath.isPresent() );

		assertTrue( pathToUrlCache.containsKey( "/path" ) );
		assertTrue( pathToUrlCache.containsKey( "/path2" ) );
		assertEquals( Optional.empty(), pathToUrlCache.get( "/path" ) );
		assertEquals( Optional.of( 646L ), pathToUrlCache.get( "/path2" ) );
	}

	@Configuration
	@AcrossTestConfiguration
	static class CacheConfig implements AcrossContextConfigurer
	{
		@Bean(name = SpringSecurityModuleCache.SECURITY_PRINCIPAL)
		public ConcurrentMapCache securityPrincipalCache() {
			return new ConcurrentMapCache( SpringSecurityModuleCache.SECURITY_PRINCIPAL );
		}

		@Bean(name = WebCmsModuleCache.PATH_TO_URL_CACHE)
		public ConcurrentMapCache userCache() {
			return new ConcurrentMapCache( WebCmsModuleCache.PATH_TO_URL_CACHE );
		}

		@Bean
		public WebCmsModule webCmsModule() {
			WebCmsModule webCmsModule = new WebCmsModule();
			webCmsModule.expose( Repository.class );
			return webCmsModule;
		}

		@Override
		public void configure( AcrossContext acrossContext ) {
			acrossContext.addApplicationContextConfigurer( new AnnotatedClassConfigurer(
					                                               CacheConfiguration.class ),
			                                               ConfigurerScope.CONTEXT_ONLY
			);

			acrossContext.getModule( WebCmsModule.NAME )
			             .addApplicationContextConfigurer( EnableCachingConfiguration.class );
		}
	}

	@EnableCaching
	static class EnableCachingConfiguration
	{
	}

	@Configuration
	static class CacheConfiguration
	{
		@Bean
		@Autowired
		@Primary
		public CacheManager cacheManager( Collection<ConcurrentMapCache> concurrentMapCacheCollection ) {
			SimpleCacheManager cacheManager = new SimpleCacheManager();
			cacheManager.setCaches( concurrentMapCacheCollection );

			return cacheManager;
		}
	}
}
