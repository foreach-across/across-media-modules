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

import com.foreach.across.modules.hibernate.aop.EntityInterceptorAdapter;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Generate primary url when page is being saved.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class WebCmsPageInterceptor extends EntityInterceptorAdapter<WebCmsPage>
{
	private final WebCmsEndpointService endpointService;

	@Override
	public boolean handles( Class<?> entityClass ) {
		return WebCmsPage.class.isAssignableFrom( entityClass );
	}

	@Override
	public void afterCreate( WebCmsPage entity ) {
		endpointService.updateOrCreatePrimaryUrlForAsset( entity.getCanonicalPath(), entity );
	}

	@Override
	public void afterUpdate( WebCmsPage entity ) {
		endpointService.updateOrCreatePrimaryUrlForAsset( entity.getCanonicalPath(), entity );
	}
}
