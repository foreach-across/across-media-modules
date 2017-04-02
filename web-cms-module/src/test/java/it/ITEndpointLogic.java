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

import com.foreach.across.modules.webcms.WebCmsModule;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Sander Van Loock
 * @since 0.0.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@AcrossWebAppConfiguration
public class ITEndpointLogic
{
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private WebCmsAssetEndpointRepository endpointRepository;

	@Autowired
	private WebCmsUrlRepository urlRepository;

	@Autowired
	private WebCmsPageRepository pageRepository;
	private WebCmsPage page;
	private WebCmsAssetEndpoint endpoint;
	private WebCmsUrl url;

	@Before
	public void setUp() throws Exception {
		page = WebCmsPage.builder()
		                 .id( 1000L )
		                 .pathSegment( "about" )
		                 .canonicalPath( "/a" )
		                 .title( "About page" )
		                 .build();
		pageRepository.save( page );

		endpoint = WebCmsAssetEndpoint.builder()
		                              .asset( page )
		                              .build();
		endpointRepository.save( endpoint );

		url = WebCmsUrl.builder()
		               .path( "/a" )
		               .httpStatus( HttpStatus.OK )
		               .primary( true )
		               .endpoint( endpoint ).build();
		urlRepository.save( url );
	}

	@Test
	public void twoUrlsWithSameEndpointWhereOneRedirectsShouldRender() throws Exception {
		WebCmsUrl redirectUrl = WebCmsUrl.builder()
		                                 .path( "/previous-a" )
		                                 .primary( false )
		                                 .httpStatus( HttpStatus.MOVED_PERMANENTLY )
		                                 .endpoint( endpoint ).build();
		urlRepository.save( redirectUrl );

		mockMvc.perform( get( "/previous-a" ) )
		       .andExpect( redirectedUrl( "/a" ) )
		       .andExpect( status().isMovedPermanently() );
		mockMvc.perform( get( "/a" ) )
		       .andExpect( status().isOk() )
		       .andExpect( content().string( containsString( page.getTitle() ) ) );
	}

	@AcrossTestConfiguration(modules = WebCmsModule.NAME)
	protected static class Config
	{
	}
}
