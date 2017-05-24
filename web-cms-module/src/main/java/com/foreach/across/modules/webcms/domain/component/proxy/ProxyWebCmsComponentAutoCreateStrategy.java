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

package com.foreach.across.modules.webcms.domain.component.proxy;

import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateService;
import com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateStrategy;
import com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateTask;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Responsible for auto-creation of proxy components.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 2)
@RequiredArgsConstructor
class ProxyWebCmsComponentAutoCreateStrategy implements WebCmsComponentAutoCreateStrategy<ProxyWebCmsComponentModel>
{
	private final WebCmsComponentModelService componentModelService;

	@Override
	public boolean supports( WebCmsComponentModel componentModel, WebCmsComponentAutoCreateTask task ) {
		return componentModel instanceof ProxyWebCmsComponentModel;
	}

	@Override
	public void buildComponentModel( WebCmsComponentAutoCreateService autoCreateService,
	                                 ProxyWebCmsComponentModel componentModel,
	                                 WebCmsComponentAutoCreateTask task ) {
		WebCmsComponentModel target = componentModelService.getComponentModel( task.getComponentName() );

		componentModel.setTarget( target );
		componentModel.setName( target.getName() );
		componentModel.setTitle( target.getTitle() );
	}
}
