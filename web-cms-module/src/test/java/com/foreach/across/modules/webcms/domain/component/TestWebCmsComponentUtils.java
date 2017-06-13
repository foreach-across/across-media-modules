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

package com.foreach.across.modules.webcms.domain.component;

import org.junit.Test;

import static com.foreach.across.modules.webcms.domain.component.WebCmsComponentType.builder;
import static com.foreach.across.modules.webcms.domain.component.WebCmsComponentUtils.getBaseType;
import static com.foreach.across.modules.webcms.domain.component.WebCmsComponentUtils.isContainerType;
import static com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel.TYPE_DYNAMIC;
import static com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel.TYPE_FIXED;
import static com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel.TYPE_ATTRIBUTE;
import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
public class TestWebCmsComponentUtils
{
	@Test
	public void baseTypeIsSameAsComponentTypeKeyIfNotSet() {
		assertEquals( "my-component-type", getBaseType( builder().typeKey( "my-component-type" ).build() ) );
	}

	@Test
	public void baseTypeIsTypeAttribute() {
		assertEquals( "container",
		              getBaseType( builder()
				                           .typeKey( "my-component-type" )
				                           .attribute( TYPE_ATTRIBUTE, "container" )
				                           .build() )
		);
	}

	@Test
	public void containerTypeIfBaseTypeIsContainer() {
		assertFalse( isContainerType( builder().typeKey( "my-component-type" ).build() ) );

		assertTrue( isContainerType( builder().typeKey( TYPE_FIXED ).build() ) );
		assertTrue( isContainerType( builder().typeKey( TYPE_DYNAMIC ).build() ) );

		assertFalse( isContainerType( builder().typeKey( TYPE_FIXED ).attribute( TYPE_ATTRIBUTE, "my-type" ).build() ) );
		assertFalse( isContainerType( builder().typeKey( TYPE_DYNAMIC ).attribute( TYPE_ATTRIBUTE, "my-type" ).build() ) );

		assertTrue( isContainerType( builder().typeKey( "my-component-type" ).attribute( TYPE_ATTRIBUTE, TYPE_FIXED ).build() ) );
		assertTrue( isContainerType( builder().typeKey( "my-component-type" ).attribute( TYPE_ATTRIBUTE, TYPE_DYNAMIC ).build() ) );
	}
}
