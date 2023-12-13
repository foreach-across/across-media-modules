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

package com.foreach.across.modules.webcms.domain.component.container;

import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.model.AbstractWebCmsComponentModelReader;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import static com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel.TYPE_DYNAMIC;
import static com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel.TYPE_FIXED;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class ContainerWebCmsComponentModelReader extends AbstractWebCmsComponentModelReader<ContainerWebCmsComponentModel>
{
	private static final String[] SUPPORTED_TYPES = new String[] { TYPE_DYNAMIC, TYPE_FIXED };

	private final WebCmsComponentModelService webCmsComponentModelService;

	@Override
	public boolean supports( WebCmsComponent component ) {
		return ArrayUtils.contains( SUPPORTED_TYPES, component.getComponentType().getAttribute( WebCmsComponentModel.TYPE_ATTRIBUTE ) );
	}

	@Override
	protected ContainerWebCmsComponentModel buildComponentModel( WebCmsComponent component ) {
		return new ContainerWebCmsComponentModel( component, webCmsComponentModelService.getComponentModelsForOwner( component, component.getDomain() ) );
	}
}
