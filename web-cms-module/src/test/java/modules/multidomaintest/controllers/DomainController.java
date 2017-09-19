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

package modules.multidomaintest.controllers;

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Locale;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
@Controller
@RequiredArgsConstructor
public class DomainController
{
	private final WebCmsMultiDomainService multiDomainService;

	@ResponseBody
	@RequestMapping("/domain")
	public String domain() {
		WebCmsDomain currentDomain = multiDomainService.getCurrentDomain();
		Locale locale = LocaleContextHolder.getLocale();

		return ( currentDomain != null ? currentDomain.getDomainKey() : "no-domain" ) + ":" + locale.toLanguageTag();
	}
}