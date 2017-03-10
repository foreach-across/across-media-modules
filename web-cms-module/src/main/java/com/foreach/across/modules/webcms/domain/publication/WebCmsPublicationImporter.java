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

import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import com.foreach.across.modules.webcms.domain.asset.AbstractWebCmsAssetImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

	public WebCmsPublicationImporter() {
		super( "publication", WebCmsPublication.class );
	}

	@Override
	protected WebCmsPublication createDto( WebCmsPublication itemToUpdate ) {
		return itemToUpdate != null ? itemToUpdate.toDto() : new WebCmsPublication();
	}

	@Override
	protected WebCmsPublication prepareForSaving( WebCmsPublication itemToBeSaved, WebCmsDataEntry data ) {
		if ( itemToBeSaved.isNew() ) {
			if ( itemToBeSaved.getPublicationKey() == null ) {
				itemToBeSaved.setPublicationKey( data.getKey() );
			}
			if ( !data.getData().containsKey( "assetKey" ) ) {
				itemToBeSaved.setAssetKey( "publication:" + itemToBeSaved.getPublicationKey() );
			}
		}
		return itemToBeSaved;
	}

	@Override
	protected WebCmsPublication getExistingByEntryKey( String entryKey ) {
		return publicationRepository.findOne( QWebCmsPublication.webCmsPublication.publicationKey.eq( entryKey ) );
	}

	@Autowired
	void setPublicationRepository( WebCmsPublicationRepository publicationRepository ) {
		this.publicationRepository = publicationRepository;
	}
}
