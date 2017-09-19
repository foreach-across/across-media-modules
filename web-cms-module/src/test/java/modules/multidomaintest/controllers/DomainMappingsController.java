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

import com.foreach.across.modules.webcms.domain.domain.web.WebCmsDomainMapping;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.web.WebCmsPageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
@ResponseBody
@Controller
public class DomainMappingsController
{
	@GetMapping("/multi-domain/be-only")
	public String fallback() {
		return "fallback";
	}

	@GetMapping("/multi-domain/be-only")
	@WebCmsDomainMapping("be-foreach")
	public String beSpecific() {
		return "specific";
	}

	@GetMapping("/multi-domain/test")
	@WebCmsDomainMapping("be-foreach")
	public String testOnBE() {
		return "test on be";
	}

	@GetMapping("/multi-domain/test")
	@WebCmsDomainMapping("de-foreach")
	public String testOnDE() {
		return "test on de";
	}

	@WebCmsPageMapping(canonicalPath = "/domain-mapped-page", domain = "be-foreach")
	public String mappedPageOnBE( WebCmsPage page ) {
		return "mapped page on be: " + page.getTitle();
	}

	@WebCmsPageMapping(canonicalPath = "/domain-mapped-page", domain = "de-foreach")
	public String mappedPageOnDE( WebCmsPage page ) {
		return "mapped page on de: " + page.getTitle();
	}
}
