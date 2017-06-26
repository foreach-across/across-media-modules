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

import com.foreach.across.modules.webcms.data.WebCmsDataAction;
import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import com.foreach.across.modules.webcms.domain.asset.AbstractWebCmsAssetImporter;
import com.foreach.across.modules.webcms.domain.page.config.WebCmsPageConfiguration;
import com.foreach.across.modules.webcms.domain.page.services.WebCmsPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
public final class WebCmsPageImporter extends AbstractWebCmsAssetImporter<WebCmsPage>
{
	private WebCmsPageService pageService;

	public WebCmsPageImporter() {
		super( "page", WebCmsPage.class );
	}

	@Override
	protected WebCmsPage createDto( WebCmsDataEntry data, WebCmsPage itemToUpdate, WebCmsDataAction action ) {
		WebCmsPage dto;

		if ( action == WebCmsDataAction.REPLACE ) {
			dto = createDefaultPageDto( data );
			dto.setObjectId( itemToUpdate.getObjectId() );
			dto.setId( itemToUpdate.getId() );
			dto.setCreatedBy( itemToUpdate.getCreatedBy() );
			dto.setCreatedDate( itemToUpdate.getCreatedDate() );
		}
		else if ( itemToUpdate != null ) {
			dto = itemToUpdate.toDto();
		}
		else {
			dto = createDefaultPageDto( data );
		}

		Map<String, Object> properties = data.getMapData();

		if ( properties.containsKey( WebCmsPageConfiguration.PATH_SEGMENT ) ) {
			dto.setPathSegmentGenerated( false );
		}
		if ( properties.containsKey( WebCmsPageConfiguration.CANONICAL_PATH ) ) {
			dto.setCanonicalPathGenerated( false );
		}

		return dto;
	}

	private WebCmsPage createDefaultPageDto( WebCmsDataEntry data ) {
		WebCmsPage page = new WebCmsPage();

		String canonicalPath = data.getKey();

		if ( canonicalPath != null ) {
			page.setCanonicalPath( canonicalPath );
			page.setCanonicalPathGenerated( false );
		}

		return page;
	}

	@Override
	protected WebCmsPage prepareForSaving( WebCmsPage itemToBeSaved, WebCmsDataEntry data ) {
		pageService.prepareForSaving( itemToBeSaved );
		return itemToBeSaved;
	}

	@Override
	protected WebCmsPage getExistingByEntryKey( String entryKey ) {
		return pageService.findByCanonicalPath( entryKey ).orElse( null );
	}

	@Autowired
	void setPageService( WebCmsPageService pageService ) {
		this.pageService = pageService;
	}
}
