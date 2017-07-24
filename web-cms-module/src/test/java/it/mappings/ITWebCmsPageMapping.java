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

import it.AbstractCmsApplicationWithTestDataIT;
import org.junit.Test;

/**
 * @author Raf Ceuls
 * @since 0.0.2
 */
public class ITWebCmsPageMapping extends AbstractCmsApplicationWithTestDataIT
{
	@Test
	public void methodLevelPageTypeMapping() {
		//page six has template 'template'
		getAndExpect( "/mappings/page/six", "methodPageTypeMapping: Mappings: Page Six" );
		//page three has template 'default'
		getAndExpect( "/mappings/page/three", "methodObjectIdMapping: Mappings: Page Three" );
		//page four has template 'template' but also has a controller method with the objectId defined.
		getAndExpect( "/mappings/page/four", "objectIdPageTypeMapping: Mappings: Page Four" );
	}

	@Test
	public void methodLevelCanonicalPathMapping() {
		getAndExpect( "/mappings/page/six", "methodPageTypeMapping: Mappings: Page Six" );
		getAndExpect( "/mappings/page/five", "methodCanonicalPathAndTypeMapping: Mappings: Page Five" );
	}
}
