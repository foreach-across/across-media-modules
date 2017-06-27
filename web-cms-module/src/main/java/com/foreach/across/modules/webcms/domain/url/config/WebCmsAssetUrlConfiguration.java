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

package com.foreach.across.modules.webcms.domain.url.config;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.query.AssociatedEntityQueryExecutor;
import com.foreach.across.modules.entity.query.EntityQuery;
import com.foreach.across.modules.entity.registry.EntityAssociation;
import com.foreach.across.modules.entity.registry.EntityFactory;
import com.foreach.across.modules.entity.views.processors.DefaultValidationViewProcessor;
import com.foreach.across.modules.entity.views.processors.PropertyRenderingViewProcessor;
import com.foreach.across.modules.entity.views.processors.SaveEntityViewProcessor;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.foreach.across.modules.webcms.domain.url.repositories.WebCmsUrlRepository;
import com.foreach.across.modules.webcms.domain.url.web.WebCmsAssetPrimaryUrlFailedFormProcessor;
import com.foreach.across.modules.webcms.domain.url.web.WebCmsAssetPrimaryUrlFailureDetectionProcessor;
import com.foreach.across.modules.webcms.domain.url.web.WebCmsAssetUrlFormProcessor;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.ClassUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Allows configuration of a single {@link com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint} for {@link WebCmsAsset}
 * implementations.  A single endpoint will behave as the single collection of URLs pointing to this asset.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Exposed
@Configuration
public class WebCmsAssetUrlConfiguration
{
	private final Set<Class<?>> assetTypes = new HashSet<>();

	/**
	 * Enable URL endpoints for a specific {@link WebCmsAsset} implementation.
	 * Note: this method should be called before the actual entity configuration happens by {@link com.foreach.across.modules.entity.EntityModule}.
	 *
	 * @param assetType to enable
	 */
	public void enable( Class<? extends WebCmsAsset> assetType ) {
		assetTypes.add( assetType );
	}

	/**
	 * Disable URL endpoints for a specific {@link WebCmsAsset} implementation.
	 * Note: this method should be called before the actual entity configuration happens by {@link com.foreach.across.modules.entity.EntityModule}.
	 *
	 * @param assetType to disable
	 */
	public void disable( Class<? extends WebCmsAsset> assetType ) {
		assetTypes.remove( assetType );
	}

	/**
	 * Are urls enabled for this particular asset?
	 *
	 * @param asset to check
	 * @return true if urls are enabled
	 */
	public boolean isEnabledForAsset( WebCmsAsset asset ) {
		return asset != null && assetTypes.contains( ClassUtils.getUserClass( asset ) );
	}

	@ConditionalOnAdminUI
	@Bean
	EntityConfigurer webCmsAssetUrlAssociationConfigurer(
			EntityFactory<WebCmsUrl> webCmsUrlEntityFactory,
			AssociatedEntityQueryExecutor<WebCmsUrl> webCmsUrlExecutorForPage,
			WebCmsAssetUrlFormProcessor urlFormProcessor,
			WebCmsAssetPrimaryUrlFailureDetectionProcessor primaryUrlFailureDetectionProcessor,
			WebCmsAssetPrimaryUrlFailedFormProcessor primaryUrlFormProcessor
	) {

		return entities ->
				entities.matching( config -> WebCmsAsset.class.isAssignableFrom( config.getEntityType() ) && assetTypes.contains( config.getEntityType() ) )
				        .association( ab -> ab.name( "urls" )
				                              .targetEntityType( WebCmsUrl.class )
				                              .targetProperty( "endpoint" )
				                              .associationType( EntityAssociation.Type.EMBEDDED )
				                              .parentDeleteMode( EntityAssociation.ParentDeleteMode.WARN )
				                              .listView( fvb -> fvb.showProperties( ".", "~endpoint" ) )
				                              .createFormView( fvb -> fvb.showProperties( ".", "~endpoint" ).viewProcessor( urlFormProcessor ) )
				                              .updateFormView( fvb -> fvb.showProperties( ".", "~endpoint" ).viewProcessor( urlFormProcessor ) )
				                              .deleteFormView()
				                              .attribute( EntityFactory.class, webCmsUrlEntityFactory )
				                              .show()
				        )
				        .createOrUpdateFormView(
						        fvb -> fvb.viewProcessor(
								        WebCmsAssetPrimaryUrlFailureDetectionProcessor.class.getName(),
								        primaryUrlFailureDetectionProcessor,
								        Ordered.LOWEST_PRECEDENCE
						        )
				        )
				        .formView(
						        "primaryUrlFailed",
						        fvb -> fvb
								        .postProcess(
										        ( factory, registry ) -> {
											        registry.remove( PropertyRenderingViewProcessor.class.getName() );
											        registry.remove( SaveEntityViewProcessor.class.getName() );
											        registry.remove( DefaultValidationViewProcessor.class.getName() );
										        }
								        )
								        .viewProcessor( primaryUrlFormProcessor )
								        .messagePrefix( "forms.primaryUrlFailed" )
				        )
				        .postProcessor(
						        cfg -> {
							        cfg.association( "urls" ).setAttribute( AssociatedEntityQueryExecutor.class, webCmsUrlExecutorForPage );

						        }
				        );
	}

	@ConditionalOnAdminUI
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

	@ConditionalOnAdminUI
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

