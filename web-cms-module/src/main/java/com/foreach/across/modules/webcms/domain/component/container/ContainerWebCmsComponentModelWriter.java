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
import com.foreach.across.modules.webcms.domain.component.model.AbstractWebCmsComponentModelWriter;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class ContainerWebCmsComponentModelWriter extends AbstractWebCmsComponentModelWriter<ContainerWebCmsComponentModel>
{
	private final WebCmsComponentModelService webCmsComponentModelService;

	@Override
	public boolean supports( WebCmsComponentModel componentModel ) {
		return ContainerWebCmsComponentModel.class.isInstance( componentModel );
	}

	@Override
	protected void buildMainComponent( ContainerWebCmsComponentModel componentModel, WebCmsComponent component ) {
		component.setBody( componentModel.getMarkup() );
	}

	@Override
	protected void afterUpdate( ContainerWebCmsComponentModel componentModel, WebCmsComponent mainComponent ) {
		List<WebCmsComponentModel> members = componentModel.getMembers();
		members.sort( Comparator.comparingInt( m -> m.getComponent().getSortIndex() ) );

		members.forEach( m -> replaceAttributesInTextComponents( componentModel, m ) );

		for ( int i = 0; i < members.size(); i++ ) {
			WebCmsComponentModel member = members.get( i );
			member.getComponent().setSortIndex( i + 1 );
			member.setOwner( componentModel );
			webCmsComponentModelService.save( member );
		}
	}

	private void replaceAttributesInTextComponents( WebCmsComponentModel container, WebCmsComponentModel componentModel ) {
		if ( componentModel.isNew() ) {
			if ( componentModel instanceof TextWebCmsComponentModel ) {
				TextWebCmsComponentModel text = (TextWebCmsComponentModel) componentModel;
				text.setContent( StringUtils.replace( text.getContent(), "@@container.title@@", container.getTitle() ) );
				text.setContent( StringUtils.replace( text.getContent(), "@@container.name@@", container.getName() ) );
			}
			else if ( componentModel instanceof ContainerWebCmsComponentModel ) {
				( (ContainerWebCmsComponentModel) componentModel ).getMembers().forEach( m -> replaceAttributesInTextComponents( componentModel, m ) );
			}
		}
	}
}
