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
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.WebCmsPageRepository;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.across.test.AcrossWebAppConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@AcrossWebAppConfiguration
public class ITPageControllerPrecedence
{
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	public void registerWebCmsPages( WebCmsPageRepository repository ) {
		if ( !repository.exists( 1000L ) ) {
			repository.save(
					WebCmsPage.builder()
					          .id( 1000L )
					          .pathSegment( "about" )
					          .canonicalPath( "/about" )
					          .title( "About page" )
					          .build()
			);
			repository.save(
					WebCmsPage.builder()
					          .pathSegment( "help" )
					          .canonicalPath( "/help" )
					          .title( "Help page" )
					          .build()
			);
		}
	}

	@Test
	public void aboutIsCustomController() throws Exception {
		mockMvc.perform( get( "/about" ) )
		       .andExpect( status().isOk() )
		       .andExpect( content().string( "about custom controller" ) );
	}

	@Test
	public void helpIsWebCmsPage() throws Exception {
		mockMvc.perform( get( "/help" ) )
		       .andExpect( status().isOk() )
		       .andExpect( content().string( containsString( "Help page" ) ) );
	}

	@Test
	public void unknownPage() throws Exception {
		mockMvc.perform( get( "/unknown/path/name" ) )
		       .andExpect( status().isNotFound() );
	}

	@AcrossTestConfiguration(modules = WebCmsModule.NAME)
	@Controller
	@ResponseBody
	protected static class Config
	{
		@RequestMapping("/about")
		public String about() {
			return "about custom controller";
		}
	}
}
