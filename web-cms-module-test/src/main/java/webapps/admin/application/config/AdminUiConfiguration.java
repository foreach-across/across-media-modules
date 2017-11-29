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

package webapps.admin.application.config;

import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.webcms.WebCmsEntityAttributes;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.component.config.WebCmsObjectComponentViewsConfiguration;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublication;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublicationType;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierLink;
import com.foreach.across.modules.webcms.domain.url.config.WebCmsAssetUrlConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import webapps.admin.application.ui.SectionComponentMetadata;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Configuration
class AdminUiConfiguration implements EntityConfigurer
{
	@Autowired
	void enableComponents( WebCmsObjectComponentViewsConfiguration componentViewsConfiguration ) {
		//componentViewsConfiguration.enable( WebCmsPublicationType.class );
		//componentViewsConfiguration.enable( WebCmsComponentType.class );

		componentViewsConfiguration.enable( WebCmsArticle.class );
		componentViewsConfiguration.enable( WebCmsPublication.class );
	}

	@Autowired
	void enableUrls( WebCmsAssetUrlConfiguration assetUrlConfiguration ) {
		assetUrlConfiguration.enable( WebCmsPublication.class );
	}

	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.withType( WebCmsDomain.class ).show();
		entities.withType( WebCmsImage.class ).attribute( WebCmsEntityAttributes.ALLOW_PER_DOMAIN, true )
		.listView( lvb -> lvb.pageSize( 5 ) );
		entities.withType( WebCmsPublicationType.class ).show();
		entities.withType( WebCmsTypeSpecifierLink.class ).show();

		entities.withType( WebCmsComponentType.class ).show();

		entities.create()
		        .entityType( SectionComponentMetadata.class, true )
		        .properties( props -> props.property( "shortTitle" ).order( -50 ) );
	}
}
