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

package com.foreach.across.modules.webcms.domain.asset.config;

import com.foreach.across.modules.bootstrapui.elements.BootstrapUiElements;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.EntityRegistry;
import com.foreach.across.modules.entity.registry.properties.EntityPropertySelector;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.asset.web.builders.WebCmsAssetEndpointViewElementBuilder;
import com.foreach.across.modules.webcms.domain.asset.web.processors.WebCmsAssetListViewProcessor;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.Printer;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Configures the default settings for asset types.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ConditionalOnAdminUI
@Configuration
@RequiredArgsConstructor
class WebCmsAssetConfiguration implements EntityConfigurer
{
	private final WebCmsAssetEndpointViewElementBuilder assetEndpointViewElementBuilder;
	private final AssetEndpointLabelPrinter assetEndpointLabelPrinter;

	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.withType( WebCmsAssetEndpoint.class )
		        .entityModel( model -> model.labelPrinter( assetEndpointLabelPrinter ) )
		        .attribute( "endpointValueBuilder", assetEndpointViewElementBuilder )
		        .hide();

		entities.assignableTo( WebCmsAsset.class )
		        .properties(
				        props -> props.property( "published" ).hidden( true ).and()
				                      .property( "publicationDate" ).hidden( true ).and()
				                      .property( "publish-settings" )
				                      .writable( true )
				                      .readable( false )
				                      .displayName( "Publish settings" )
				                      .viewElementType( ViewElementMode.FORM_WRITE, BootstrapUiElements.FIELDSET )
				                      .viewElementType( ViewElementMode.FORM_READ, BootstrapUiElements.FIELDSET )
				                      .attribute(
						                      EntityAttributes.FIELDSET_PROPERTY_SELECTOR,
						                      EntityPropertySelector.of( "published", "publicationDate" )
				                      )
		        )
		        .updateFormView( fvb -> fvb.showProperties( ".", "~created" ) )
		        .listView( lvb -> lvb.viewProcessor( new WebCmsAssetListViewProcessor() ) );
	}

	@ConditionalOnAdminUI
	@Component
	@RequiredArgsConstructor
	private static class AssetEndpointLabelPrinter implements Printer<WebCmsAssetEndpoint>
	{
		private final EntityRegistry entityRegistry;

		@Override
		public String print( WebCmsAssetEndpoint endpoint, Locale locale ) {
			val asset = endpoint.getAsset();

			EntityConfiguration<WebCmsAsset> entityConfiguration = entityRegistry.getEntityConfiguration( asset );
			String name = entityConfiguration.getEntityMessageCodeResolver().getNameSingular();

			return name + ": " + entityConfiguration.getLabel( asset );
		}
	}
}
