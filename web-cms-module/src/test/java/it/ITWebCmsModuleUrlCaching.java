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
import com.foreach.across.modules.webcms.WebCmsModule;
import com.foreach.across.modules.webcms.WebCmsModuleCache;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpointService;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageRepository;
import com.foreach.across.modules.webcms.domain.page.services.WebCmsPageService;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.foreach.across.modules.webcms.domain.url.repositories.WebCmsUrlRepository;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.across.test.AcrossWebAppConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.NullValue;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 */
@ExtendWith(SpringExtension.class)
@AcrossWebAppConfiguration
@ContextConfiguration(classes = ITWebCmsModuleUrlCaching.CacheConfig.class)
class ITWebCmsModuleUrlCaching
{
	@Autowired
	private WebCmsEndpointService webCmsEndpointService;

	@Autowired
	private WebCmsUrlRepository repository;

	@Autowired
	private WebCmsPageRepository pageRepository;

	@Autowired
	private WebCmsAssetEndpointRepository endpointRepository;

	@Autowired
	private WebCmsPageService pageService;

	private Map<Object, Object> pathToUrlCache;
	private WebCmsAssetEndpoint endpoint;

	@Autowired
	void registerCaches( @Qualifier(WebCmsModuleCache.PATH_TO_URL_ID) ConcurrentMapCache pathToUrlCache ) {
		this.pathToUrlCache = pathToUrlCache.getNativeCache();
	}

	@BeforeEach
	void before() {
		pathToUrlCache.clear();

		WebCmsPage page = WebCmsPage.builder()
		                            .id( 1000L )
		                            .pathSegment( "about" )
		                            .canonicalPath( "/a" )
		                            .title( "About page" )
		                            .pageType( pageService.getPageTypeByKey( "default" ) )
		                            .published( true )
		                            .build();
		pageRepository.save( page );

		endpoint = endpointRepository.findOneByAssetAndDomain( page, WebCmsDomain.NONE ).orElse( null );
	}

	@Test
	void idOfMissingWebCmsUrlGetsCached() {
		assertTrue( pathToUrlCache.isEmpty() );

		Optional<WebCmsUrl> url = webCmsEndpointService.getUrlForPath( "/path" );

		assertFalse( url.isPresent() );
		assertTrue( pathToUrlCache.containsKey( "no-domain:/path" ) );
		assertTrue( pathToUrlCache.get( "no-domain:/path" ) instanceof NullValue );
	}

	@Test
	void createUrlAndGetItFromCache() {
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
		assertTrue( pathToUrlCache.containsKey( "no-domain:/path" ) );
		assertEquals( 646L, pathToUrlCache.get( "no-domain:/path" ) );
	}

	@Test
	void createAndDeleteUrlAndGetItFromCache() {
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
		assertTrue( pathToUrlCache.containsKey( "no-domain:/path" ) );
		assertEquals( 646L, pathToUrlCache.get( "no-domain:/path" ) );

		repository.delete( webCmsUrl );
		assertTrue( pathToUrlCache.isEmpty() );

		url = webCmsEndpointService.getUrlForPath( "/path" );

		assertFalse( url.isPresent() );
	}

	@Test
	void createAndUpdateUrlAndGetItFromCache() {
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
		assertTrue( pathToUrlCache.containsKey( "no-domain:/path" ) );
		assertEquals( 646L, pathToUrlCache.get( "no-domain:/path" ) );

		webCmsUrl.setPath( "/path2" );
		repository.save( webCmsUrl );
		assertTrue( pathToUrlCache.isEmpty() );

		Optional<WebCmsUrl> oldPath = webCmsEndpointService.getUrlForPath( "/path" );
		assertFalse( oldPath.isPresent() );

		Optional<WebCmsUrl> newPath = webCmsEndpointService.getUrlForPath( "/path2" );
		assertTrue( newPath.isPresent() );

		assertTrue( pathToUrlCache.containsKey( "no-domain:/path" ) );
		assertTrue( pathToUrlCache.containsKey( "no-domain:/path2" ) );
		assertTrue( pathToUrlCache.get( "no-domain:/path" ) instanceof NullValue );
		assertEquals( 646L, pathToUrlCache.get( "no-domain:/path2" ) );
	}

	@Configuration
	@AcrossTestConfiguration(modules = WebCmsModule.NAME)
	static class CacheConfig implements AcrossContextConfigurer
	{
		@Bean(name = WebCmsModuleCache.PATH_TO_URL_ID)
		public ConcurrentMapCache userCache() {
			return new ConcurrentMapCache( WebCmsModuleCache.PATH_TO_URL_ID );
		}

		@Override
		public void configure( AcrossContext acrossContext ) {
			acrossContext.addApplicationContextConfigurer( new AnnotatedClassConfigurer( CacheConfiguration.class ), ConfigurerScope.CONTEXT_ONLY );
			acrossContext.getModule( WebCmsModule.NAME )
			             .addApplicationContextConfigurer( EnableCachingConfiguration.class );
		}
	}

	@EnableCaching
	private static class EnableCachingConfiguration
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