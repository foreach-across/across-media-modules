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

package com.foreach.across.modules.webcms.domain.url;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

/**
 * @author Steven Gentens
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsUrl
{
	@Test
	public void pathStartsWithSlash() {
		WebCmsUrl url = new WebCmsUrl();
		url.setPath( "my-path" );
		assertEquals( "/my-path", url.getPath() );

		url = new WebCmsUrl();
		url.setPath( "/my-path" );
		assertEquals( "/my-path", url.getPath() );
	}

	@Test
	public void pathStartsWithSlashuilder() {
		WebCmsUrl url = WebCmsUrl.builder().path( "my-path" ).build();
		assertEquals( "/my-path", url.getPath() );

		url = WebCmsUrl.builder().path( "/my-path" ).build();
		assertEquals( "/my-path", url.getPath() );
	}
}
