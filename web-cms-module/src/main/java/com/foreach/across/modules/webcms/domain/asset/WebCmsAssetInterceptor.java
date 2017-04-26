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

package com.foreach.across.modules.webcms.domain.asset;

import com.foreach.across.modules.hibernate.aop.EntityInterceptorAdapter;
import com.foreach.across.modules.webcms.config.web.admin.WebCmsAssetUrlConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Sets the publication date and ensures that there is a single endpoint pointing to the asset.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
@RequiredArgsConstructor
public class WebCmsAssetInterceptor extends EntityInterceptorAdapter<WebCmsAsset>
{
	private final WebCmsAssetEndpointRepository endpointRepository;
	private final WebCmsAssetUrlConfiguration urlConfiguration;

	@Override
	public boolean handles( Class<?> entityClass ) {
		return WebCmsAsset.class.isAssignableFrom( entityClass );
	}

	@Override
	public void beforeCreate( WebCmsAsset entity ) {
		setPublicationDate( entity );
	}

	@Override
	public void beforeUpdate( WebCmsAsset entity ) {
		setPublicationDate( entity );
	}

	private void setPublicationDate( WebCmsAsset asset ) {
		if ( asset.isPublished() && asset.getPublicationDate() == null ) {
			asset.setPublicationDate( new Date() );
		}
	}

	@Override
	public void afterCreate( WebCmsAsset entity ) {
		if ( urlConfiguration.isEnabledForAsset( entity ) ) {
			val endpoint = new WebCmsAssetEndpoint<WebCmsAsset>();
			endpoint.setAsset( entity );
			endpointRepository.save( endpoint );
		}
	}

	@Override
	public void afterUpdate( WebCmsAsset entity ) {
		if ( urlConfiguration.isEnabledForAsset( entity ) ) {
			WebCmsAssetEndpoint<?> endpoint = endpointRepository.findOneByAsset( entity );

			if ( endpoint == null ) {
				afterCreate( entity );
			}
		}
	}

	@Override
	public void beforeDelete( WebCmsAsset entity ) {
		if ( urlConfiguration.isEnabledForAsset( entity ) ) {
			WebCmsAssetEndpoint endpoint = endpointRepository.findOneByAsset( entity );
			endpointRepository.delete( endpoint );
		}
	}
}
