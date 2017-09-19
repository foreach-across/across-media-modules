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

package com.foreach.across.modules.webcms.domain.publication;

import com.foreach.across.modules.webcms.data.WebCmsDataAction;
import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import com.foreach.across.modules.webcms.domain.asset.AbstractWebCmsAssetImporter;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.publication.web.WebCmsPublicationValidator;
import com.querydsl.core.BooleanBuilder;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Map;

/**
 * Takes care of importing {@link WebCmsPublication} assets.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
public final class WebCmsPublicationImporter extends AbstractWebCmsAssetImporter<WebCmsPublication>
{
	private WebCmsPublicationRepository publicationRepository;
	private WebCmsPublicationValidator publicationValidator;

	public WebCmsPublicationImporter() {
		super( "publication", WebCmsPublication.class );
	}

	@Override
	protected WebCmsPublication createDto( WebCmsDataEntry data, WebCmsPublication itemToUpdate, WebCmsDataAction action, Map<String, Object> dataValues ) {
		if ( action == WebCmsDataAction.REPLACE ) {
			return WebCmsPublication.builder()
			                        .id( itemToUpdate.getId() ).createdBy( itemToUpdate.getCreatedBy() ).createdDate( itemToUpdate.getCreatedDate() )
			                        .objectId( itemToUpdate.getObjectId() )
			                        .build();
		}

		return itemToUpdate != null ? itemToUpdate.toDto() : new WebCmsPublication();

	}

	@Override
	protected WebCmsPublication prepareForSaving( WebCmsPublication itemToBeSaved, WebCmsDataEntry data ) {
		if ( itemToBeSaved.isNew() ) {
			if ( itemToBeSaved.getPublicationKey() == null ) {
				itemToBeSaved.setPublicationKey( data.getKey() );
			}
		}
		return itemToBeSaved;
	}

	@Override
	protected WebCmsPublication getExistingEntity( String entryKey, WebCmsDataEntry data, WebCmsDomain domain ) {
		if ( StringUtils.isEmpty( entryKey ) ) {
			if ( !data.getMapData().containsKey( "publicationKey" ) ) {
				return null;
			}
			entryKey = (String) data.getMapData().get( "publicationKey" );
		}
		val query = QWebCmsPublication.webCmsPublication;
		BooleanBuilder builder = new BooleanBuilder();
		if ( domain != null ) {
			builder.and( query.domain.eq( domain ) );
		}
		return publicationRepository.findOne( builder.and( query.publicationKey.eq( entryKey ) ) );
	}

	@Override
	protected void validate( WebCmsPublication itemToBeSaved, Errors errors ) {
		publicationValidator.validate( itemToBeSaved, errors );
	}

	@Autowired
	void setPublicationRepository( WebCmsPublicationRepository publicationRepository ) {
		this.publicationRepository = publicationRepository;
	}

	@Autowired
	void setPublicationValidator( WebCmsPublicationValidator publicationValidator ) {
		this.publicationValidator = publicationValidator;
	}
}
