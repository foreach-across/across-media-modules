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
import com.foreach.across.modules.webcms.infrastructure.ModificationType;

/**
 * Holds the possible modification types that can be returned when calling
 * {@link WebCmsPageService#prepareForSaving(WebCmsPage)}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
public enum PrepareModificationType implements ModificationType
{
	PATH_SEGMENT_GENERATED,
	CANONICAL_PATH_GENERATED,
	PAGE_TYPE_ASSIGNED
}
