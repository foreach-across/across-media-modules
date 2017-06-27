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
 * @author Arne Vandamme
 * @since 0.0.2
 */
public class ITWebCmsAssetMapping extends AbstractSingleApplicationIT
{
	@Test
	public void defaultPageMapping() {
		getAndExpect( "/mappings/page/one", "defaultPageMapping: Mappings: Page One" );
	}

	@Test
	public void defaultArticleMapping() {
		getAndExpect( "/news/mappings-article-one", "defaultArticleMapping: Mappings: Article One" );
	}

	@Test
	public void controllerLevelObjectIdMapping() {
		getAndExpect( "/mappings/page/two", "controllerObjectIdMapping: Mappings: Page Two" );
		getAndExpect( "/news/mappings-article-two", "controllerObjectIdMapping: Mappings: Article Two" );
	}

	@Test
	public void methodLevelObjectIdMapping() {
		getAndExpect( "/mappings/page/three", "methodObjectIdMapping: Mappings: Page Three" );
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
