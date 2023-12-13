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

package com.foreach.across.modules.webcms.domain.component.placeholder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
public class TestWebCmsPlaceholderContentModel
{
	private WebCmsPlaceholderContentModel model;

	@BeforeEach
	public void setUp() throws Exception {
		model = new WebCmsPlaceholderContentModel();
	}

	@Test
	public void contentNotFound() {
		assertEquals( Optional.empty(), model.getPlaceholderContent( "my-placeholder" ) );
	}

	@Test
	public void contentFound() {
		model.setPlaceholderContent( "placeholder", 123L );
		assertEquals( Optional.of( 123L ), model.getPlaceholderContent( "placeholder" ) );
	}

	@Test
	public void decreaseLevelClearsAll() {
		model.setPlaceholderContent( "placeholder", 123L );
		model.decreaseLevel();
		assertEquals( Optional.empty(), model.getPlaceholderContent( "placeholder" ) );
		model.setPlaceholderContent( "placeholder", 123L );
		assertEquals( Optional.of( 123L ), model.getPlaceholderContent( "placeholder" ) );
	}

	@Test
	public void increaseLevelShadowsPreviousValue() {
		model.setPlaceholderContent( "placeholder", 123L );
		assertEquals( Optional.of( 123L ), model.getPlaceholderContent( "placeholder" ) );
		model.increaseLevel();
		assertEquals( Optional.of( 123L ), model.getPlaceholderContent( "placeholder" ) );
		model.setPlaceholderContent( "placeholder", "my data" );
		assertEquals( Optional.of( "my data" ), model.getPlaceholderContent( "placeholder" ) );
		model.increaseLevel();
		assertEquals( Optional.of( "my data" ), model.getPlaceholderContent( "placeholder" ) );
		model.decreaseLevel();
		model.decreaseLevel();
		assertEquals( Optional.of( 123L ), model.getPlaceholderContent( "placeholder" ) );
		model.decreaseLevel();
		assertEquals( Optional.empty(), model.getPlaceholderContent( "placeholder" ) );
	}

	@Test
	public void shadowingWithNullValuesIsPossible() {
		model.setPlaceholderContent( "placeholder", 123L );
		assertEquals( Optional.of( 123L ), model.getPlaceholderContent( "placeholder" ) );
		model.increaseLevel();
		model.setPlaceholderContent( "placeholder", null );
		assertEquals( Optional.empty(), model.getPlaceholderContent( "placeholder" ) );
		model.decreaseLevel();
		assertEquals( Optional.of( 123L ), model.getPlaceholderContent( "placeholder" ) );
	}
}
