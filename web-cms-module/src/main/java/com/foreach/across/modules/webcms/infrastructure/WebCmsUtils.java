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

package com.foreach.across.modules.webcms.infrastructure;

import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.text.Normalizer;
import java.util.UUID;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class WebCmsUtils
{
	private WebCmsUtils() {
	}

	/**
	 * Generate a unique asset id for the given collection.  A collection is represented by an id
	 * (eg. wcm:asset:page) and a unique id with the collection id as prefix will be returned.
	 *
	 * @param collectionId the asset id should have
	 * @return unique id
	 */
	public static String generateAssetId( String collectionId ) {
		Assert.notNull( collectionId );
		return prefixAssetIdForCollection( UUID.randomUUID().toString(), collectionId );
	}

	/**
	 * Ensures an asset id is for a given collection.  If the asset id starts with the collection id,
	 * it will be left unchanged, else the collection id will be prefixed.
	 *
	 * @param assetId requested
	 * @param collectionId the asset id should have
	 * @return collection prefixed asset id
	 */
	public static String prefixAssetIdForCollection( String assetId, String collectionId ) {
		Assert.notNull( collectionId );
		Assert.notNull( assetId );
		return StringUtils.startsWith( assetId, collectionId + ":" ) ? assetId : collectionId + ":" + assetId;
	}

	/**
	 * Convert text into a valid path segment for url.
	 * This will make an seo friendly path.
	 *
	 * @param text to convert
	 * @return path segment
	 */
	public static String generateUrlPathSegment( String text ) {
		Assert.notNull( text );

		return StringUtils.removeStart(
				StringUtils.removeEnd(
						Normalizer.normalize( StringUtils.lowerCase( text ), Normalizer.Form.NFD )
						          .replaceAll( "\\p{InCombiningDiacriticalMarks}+", "" )
						          .replaceAll( "[^\\p{Alnum}]+", "-" ),
						"-" ),
				"-" );
	}

	/**
	 * Generates a unique canonical path for a {@link WebCmsPage}.
	 * The canonical path is the canonical path of its (optional) parent joined together with its own
	 * path segment using a / character (forward slash).
	 *
	 * @param page to generate the path for
	 * @return canonical path
	 */
	public static String generateCanonicalPath( WebCmsPage page ) {
		Assert.notNull( page );

		WebCmsPage parent = page.getParent();

		if ( parent != null ) {
			return StringUtils.removeEnd( parent.getCanonicalPath(), "/" )
					+ "/" + StringUtils.defaultString( page.getPathSegment() );
		}

		return "/" + StringUtils.defaultString( page.getPathSegment() );
	}
}
