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

package com.foreach.across.modules.webcms.domain.type.web;

import com.foreach.across.modules.entity.views.events.BuildEntityDeleteViewEvent;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.type.QWebCmsTypeSpecifierLink;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifier;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static com.foreach.across.modules.web.ui.elements.HtmlViewElements.html;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
@ConditionalOnAdminUI
@Component
@RequiredArgsConstructor
class TypeLinkDeleteViewEventHandler
{
	private final WebCmsTypeSpecifierLinkRepository linkRepository;

	@EventListener
	void handleTypeBeingDeleted( BuildEntityDeleteViewEvent<WebCmsTypeSpecifier> deleteViewEvent ) {
		QWebCmsTypeSpecifierLink query = QWebCmsTypeSpecifierLink.webCmsTypeSpecifierLink;
		long nonSelfLinks = linkRepository.count(
				query.typeSpecifier.eq( deleteViewEvent.getEntity() ).and( query.ownerObjectId.ne( deleteViewEvent.getEntity().getObjectId() ) )
		);

		if ( nonSelfLinks > 0 ) {
			ViewElementBuilderContext builderContext = deleteViewEvent.getBuilderContext();

			deleteViewEvent.setDeleteDisabled( true );
			deleteViewEvent.associations()
			               .addChild(
					               html.builders
							               .li()
							               .add(
									               html.unescapedText(
											               builderContext
													               .getMessage( "objectsLinkedToTypeSpecifier",
													                            new Object[] { nonSelfLinks } )
									               )
							               )
							               .build( builderContext )
			               );
		}
	}
}
