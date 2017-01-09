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

package com.foreach.across.modules.webcms.domain.page.validators;

import com.foreach.across.modules.entity.validators.EntityValidatorSupport;
import com.foreach.across.modules.webcms.domain.page.WebCmsPageSection;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageSectionRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
class WebCmsPageSectionValidator extends EntityValidatorSupport<WebCmsPageSection>
{
	private WebCmsPageSectionRepository sectionRepository;

	@Override
	public boolean supports( Class<?> aClass ) {
		return WebCmsPageSection.class.isAssignableFrom( aClass );
	}

	@Override
	protected void postValidation( WebCmsPageSection section, Errors errors ) {
		if ( !errors.hasFieldErrors( "name" ) && !errors.hasFieldErrors( "page" ) ) {
			if ( sectionRepository.findAllByPageOrderBySortIndexAscNameAsc( section.getPage() )
			                      .stream()
			                      .anyMatch(
					                      existing ->
							                      StringUtils.equalsIgnoreCase( existing.getName(), section.getName() )
									                      && !existing.equals( section )
			                      ) ) {
				errors.rejectValue( "name", "alreadyExists" );
			}
		}
	}

	@Autowired
	void setSectionRepository( WebCmsPageSectionRepository sectionRepository ) {
		this.sectionRepository = sectionRepository;
	}
}
