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

package com.foreach.across.modules.webcms.domain.page.services;

import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.infrastructure.ModificationReport;
import com.foreach.across.modules.webcms.infrastructure.ModificationType;
import com.foreach.across.modules.webcms.infrastructure.WebCmsUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.foreach.across.modules.webcms.domain.page.services.PrepareModificationType.CANONICAL_PATH_GENERATED;
import static com.foreach.across.modules.webcms.domain.page.services.PrepareModificationType.PATH_SEGMENT_GENERATED;

/**
 * Takes care of preparing {@link WebCmsPage} instances.  Generates all properties that need generating
 * and will return a modification report for all actions taken.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
class PagePropertyGenerator
{
	Map<ModificationType, ModificationReport> prepareForSaving( WebCmsPage page ) {
		val modifications = new HashMap<ModificationType, ModificationReport>();
		generatePathSegment( modifications, page );
		generateCanonicalPath( modifications, page );
		return modifications;
	}

	private void generatePathSegment( Map<ModificationType, ModificationReport> modifications, WebCmsPage page ) {
		if ( page.isPathSegmentGenerated() ) {
			String oldSegment = page.getPathSegment();
			String newSegment = WebCmsUtils.generateUrlPathSegment( StringUtils.defaultString( page.getTitle() ) );
			if ( !newSegment.equals( oldSegment ) ) {
				page.setPathSegment( newSegment );
				modifications.put(
						PATH_SEGMENT_GENERATED,
						new ModificationReport<>( PATH_SEGMENT_GENERATED, oldSegment, newSegment )
				);
			}
		}
	}

	private void generateCanonicalPath( Map<ModificationType, ModificationReport> modifications, WebCmsPage page ) {
		if ( page.isCanonicalPathGenerated() ) {
			String oldCanonicalPath = page.getCanonicalPath();
			String newCanonicalPath = WebCmsUtils.generateCanonicalPath( page );
			if ( !newCanonicalPath.equals( oldCanonicalPath ) ) {
				page.setCanonicalPath( newCanonicalPath );
				modifications.put(
						CANONICAL_PATH_GENERATED,
						new ModificationReport<>( CANONICAL_PATH_GENERATED, oldCanonicalPath, newCanonicalPath )
				);
			}
		}
	}
}
