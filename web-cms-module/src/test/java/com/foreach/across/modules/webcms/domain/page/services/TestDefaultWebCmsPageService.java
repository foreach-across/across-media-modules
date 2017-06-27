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

package com.foreach.across.modules.webcms.domain.page.services;

import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageRepository;
import com.foreach.across.modules.webcms.infrastructure.ModificationReport;
import com.foreach.across.modules.webcms.infrastructure.ModificationType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultWebCmsPageService
{
	@Mock
	private WebCmsPageRepository pageRepository;

	@Mock
	private PagePropertyGenerator pagePreparator;

	@Mock
	private WebCmsPage page;

	@InjectMocks
	private DefaultWebCmsPageService pageService;

	@Test
	public void findByCanonicalPath() {
		when( pageRepository.findOneByCanonicalPath( "custom path" ) ).thenReturn( page );
		assertEquals( Optional.of( page ), pageService.findByCanonicalPath( "custom path" ) );
	}

	@Test
	public void prepareForSavingDispatchesToPreparator() {
		Map<ModificationType, ModificationReport<PrepareModificationType,Object>> modifications = Collections.emptyMap();
		when( pagePreparator.prepareForSaving( page ) ).thenReturn( modifications );
		assertSame( modifications, pageService.prepareForSaving( page ) );
	}
}
