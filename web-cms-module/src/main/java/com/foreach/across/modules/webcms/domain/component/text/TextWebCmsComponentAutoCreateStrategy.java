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

package com.foreach.across.modules.webcms.domain.component.text;

import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateService;
import com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateStrategy;
import com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateTask;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Responsible for auto-creation of simple text components.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class TextWebCmsComponentAutoCreateStrategy implements WebCmsComponentAutoCreateStrategy<TextWebCmsComponentModel>
{
	@Override
	public boolean supports( WebCmsComponentModel componentModel, WebCmsComponentAutoCreateTask task ) {
		return componentModel instanceof TextWebCmsComponentModel;
	}

	@Override
	public void buildComponentModel( WebCmsComponentAutoCreateService autoCreateService,
	                                 TextWebCmsComponentModel componentModel,
	                                 WebCmsComponentAutoCreateTask task ) {
		componentModel.setContent( StringUtils.trim( task.getOutput() ) );
	}
}
