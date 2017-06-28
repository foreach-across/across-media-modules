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

package it.mappings;

import it.AbstractSingleApplicationIT;
import lombok.SneakyThrows;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Raf Ceuls
 * @since 0.0.2
 */
public class ITWebCmsPageMapping extends AbstractSingleApplicationIT
{
	@Test
	public void methodLevelPageTypeMapping() {
		//page four has template 'template'
		getAndExpect( "/mappings/page/four", "methodPageTypeMapping: Mappings: Page Four" );
		//page three has template 'default'
		getAndExpect( "/mappings/page/three", "methodObjectIdMapping: Mappings: Page Three" );
	}

	@Test
	public void methodLevelCanonicalPathMapping() {
		getAndExpect( "/mappings/page/four", "methodPageTypeMapping: Mappings: Page Four" );
		getAndExpect( "/mappings/page/five", "methodCanonicalPathAndTypeMapping: Mappings: Page Five" );
	}

	@SneakyThrows
	private void getAndExpect( String path, String content ) {
		assertEquals(
				content,
				mockMvc.perform( get( path ) )
				       .andExpect( status().isOk() )
				       .andReturn()
				       .getResponse()
				       .getContentAsString()
		);
	}
}
