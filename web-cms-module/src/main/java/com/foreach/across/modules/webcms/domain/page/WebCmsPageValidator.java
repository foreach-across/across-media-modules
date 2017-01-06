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

package com.foreach.across.modules.webcms.domain.page;

import com.foreach.across.modules.entity.validators.EntityValidatorSupport;
import com.foreach.across.modules.webcms.domain.page.services.WebCmsPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * Validator for {@link WebCmsPage}.  Applies {@link WebCmsPageService#prepareForSaving(WebCmsPage)}
 * before performing validation, this prepares the instance based on the settings.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
class WebCmsPageValidator extends EntityValidatorSupport<WebCmsPage>
{
	private WebCmsPageService pageService;

	@Override
	public boolean supports( Class<?> clazz ) {
		return WebCmsPage.class.isAssignableFrom( clazz );
	}

	@Override
	protected void preValidation( WebCmsPage entity, Errors errors ) {
		pageService.prepareForSaving( entity );
	}

	@Autowired
	void setPageService( WebCmsPageService pageService ) {
		this.pageService = pageService;
	}
}
