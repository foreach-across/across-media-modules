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
import com.foreach.across.modules.webcms.domain.component.model.AbstractWebCmsComponentModelWriter;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Responsible for persisting a {@link ProxyWebCmsComponentModel}.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Component
@RequiredArgsConstructor
public class ProxyWebCmsComponentModelWriter extends AbstractWebCmsComponentModelWriter<ProxyWebCmsComponentModel>
{
	private final WebCmsComponentModelService webCmsComponentModelService;

	@Override
	public boolean supports( WebCmsComponentModel componentModel ) {
		return ProxyWebCmsComponentModel.class.isInstance( componentModel );
	}

	@Override
	protected void buildMainComponent( ProxyWebCmsComponentModel componentModel, WebCmsComponent component ) {
		component.setProxyTarget( retrieveTargetComponent( componentModel.getTarget() ) );
	}

	private WebCmsComponent retrieveTargetComponent( WebCmsComponentModel targetModel ) {
		if ( targetModel != null ) {
			if ( targetModel.isNew() ) {
				return webCmsComponentModelService.save( targetModel );
			}

			return targetModel.getComponent();
		}

		return null;
	}
}
