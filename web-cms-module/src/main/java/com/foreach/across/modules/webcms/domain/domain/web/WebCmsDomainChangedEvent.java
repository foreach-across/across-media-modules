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

package com.foreach.across.modules.webcms.domain.domain.web;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Event that is published by {@link com.foreach.across.modules.webcms.domain.domain.web.CookieWebCmsDomainContextResolver}
 * when the domain has changed in a multi-domain setup.
 *
 * @author Marc Vanbrabant
 * @since 0.0.7
 */
@AllArgsConstructor
@Data
public class WebCmsDomainChangedEvent
{
	private final String currentDomain;
	private final String newDomain;
}
