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

package com.foreach.across.modules.webcms.config.web.admin;

import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.query.AssociatedEntityQueryExecutor;
import com.foreach.across.modules.entity.query.EntityQuery;
import com.foreach.across.modules.entity.registry.EntityAssociation;
import com.foreach.across.modules.entity.registry.EntityFactory;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.foreach.across.modules.webcms.domain.url.repositories.WebCmsUrlRepository;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Configuration
public class WebCmsUrlsConfiguration implements EntityConfigurer
{
	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		// test code for custom url tabs
		entities.assignableTo( WebCmsAsset.class )
		        .association( ab -> ab.name( "urls" )
		                              .targetEntityType( WebCmsUrl.class )
		                              .targetProperty( "endpoint" )
		                              .associationType( EntityAssociation.Type.EMBEDDED )
		                              .listView( fvb -> fvb.showProperties( ".", "~endpoint" ) )
		                              .createFormView( fvb -> fvb.showProperties( ".", "~endpoint" ) )
		                              .updateFormView( fvb -> fvb.showProperties( ".", "~endpoint" ) )
		                              .deleteFormView()
		                              .attribute( EntityFactory.class, webCmsUrlEntityFactory( null ) )
		                              .show()
		        )
		        .postProcessor(
				        cfg -> {
					        cfg.association( "urls" ).setAttribute( AssociatedEntityQueryExecutor.class, webCmsUrlExecutorForPage( null, null ) );
				        }

		        );
	}

	@Bean
	EntityFactory<WebCmsUrl> webCmsUrlEntityFactory( WebCmsAssetEndpointRepository endpointRepository ) {
		return new EntityFactory<WebCmsUrl>()
		{
			@Override
			public WebCmsUrl createNew( Object... args ) {
				WebCmsUrl url = new WebCmsUrl();
				if ( args[0] instanceof WebCmsAsset ) {
					url.setEndpoint( endpointRepository.findOneByAsset( (WebCmsAsset) args[0] ) );
				}

				return url;
			}

			@Override
			public WebCmsUrl createDto( WebCmsUrl entity ) {
				return entity.toDto();
			}
		};
	}

	@Bean
	AssociatedEntityQueryExecutor<WebCmsUrl> webCmsUrlExecutorForPage( WebCmsAssetEndpointRepository pageEndpointRepository,
	                                                                   WebCmsUrlRepository urlRepository ) {
		return new AssociatedEntityQueryExecutor<WebCmsUrl>( null, null )
		{
			@Override
			public List<WebCmsUrl> findAll( Object parent, EntityQuery query ) {
				val endpoint = pageEndpointRepository.findOneByAsset( (WebCmsAsset) parent );
				return urlRepository.findAllByEndpoint( endpoint );
			}

			@Override
			public Page<WebCmsUrl> findAll( Object parent, EntityQuery query, Pageable pageable ) {
				val endpoint = pageEndpointRepository.findOneByAsset( (WebCmsAsset) parent );
				return urlRepository.findAllByEndpoint( endpoint, pageable );
			}
		};
	}
}
