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

import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.model.AbstractWebCmsComponentModelReader;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Builds a {@link ProxyWebCmsComponentModel} for any component where {@link WebCmsComponent#isProxyComponent()} returns {@code true}.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Component
@RequiredArgsConstructor
public class ProxyWebCmsComponentModelReader extends AbstractWebCmsComponentModelReader<ProxyWebCmsComponentModel>
{
	private final WebCmsComponentModelService webCmsComponentModelService;

	@Override
	public boolean supports( WebCmsComponent component ) {
		return component.isProxyComponent()
				|| ProxyWebCmsComponentModel.TYPE.equals( component.getComponentType().getAttribute( WebCmsComponentModel.TYPE_ATTRIBUTE ) );
	}

	@Override
	protected ProxyWebCmsComponentModel buildComponentModel( WebCmsComponent component ) {
		return new ProxyWebCmsComponentModel(
				component, component.isProxyComponent() ? webCmsComponentModelService.buildModelForComponent( component.getProxyTarget() ) : null
		);
	}
}
