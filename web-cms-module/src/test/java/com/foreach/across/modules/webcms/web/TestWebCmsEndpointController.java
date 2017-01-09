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

package com.foreach.across.modules.webcms.web;

import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.services.WebCmsPageService;
import com.foreach.across.modules.webcms.web.page.template.PageTemplateResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsEndpointController
{
	@Mock
	private WebCmsPageService pageService;

	@Mock
	private PageTemplateResolver pageTemplateResolver;

	@InjectMocks
	private WebCmsEndpointController controller;

	private MockMvc mockMvc;

	@Before
	public void setUp() {
		mockMvc = standaloneSetup( controller ).build();
	}

	@Test
	public void pageNotFound() throws Exception {
		when( pageService.findByCanonicalPath( anyString() ) ).thenReturn( Optional.empty() );

		mockMvc.perform( get( "/page-segment" ) )
		       .andExpect( status().isNotFound() );
	}

	@Test
	public void existingPage() throws Exception {
		WebCmsPage page = mock( WebCmsPage.class );
		when( page.getTemplate() ).thenReturn( "test" );
		when( pageService.findByCanonicalPath( "/page-segment" ) ).thenReturn( Optional.of( page ) );
		when( pageService.retrieveContentSections( page ) ).thenReturn( Collections.emptyMap() );

		when( pageTemplateResolver.resolvePageTemplate( "test" ) ).thenReturn( "resolvedView" );

		mockMvc.perform( get( "/page-segment" ) )
		       .andExpect( status().isOk() )
		       .andExpect( view().name( "resolvedView" ) )
		       .andExpect( model().attribute( "page", page ) )
		       .andExpect( model().attribute( "contentSections", Collections.emptyMap() ) );

	}
}
