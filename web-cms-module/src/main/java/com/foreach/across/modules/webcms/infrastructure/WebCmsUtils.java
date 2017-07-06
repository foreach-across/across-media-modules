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

import com.foreach.across.modules.webcms.data.WebCmsDataAction;
import com.foreach.across.modules.webcms.data.WebCmsDataImportAction;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.text.Normalizer;
import java.util.Date;
import java.util.UUID;

import static com.foreach.across.modules.webcms.data.WebCmsDataAction.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class WebCmsUtils
{
	private WebCmsUtils() {
	}

	/**
	 * Is the given asset published on a particular date.
	 * This means that the {@link WebCmsAsset#isPublished()} returns {@code true} and the {@link WebCmsAsset#getPublicationDate()}
	 * is either {@code null} or before the date parameter.
	 *
	 * @param asset to check
	 * @param date  to check for
	 * @return true if asset is published
	 */
	public static boolean isPublishedOnDate( WebCmsAsset asset, Date date ) {
		Assert.notNull( asset );
		Assert.notNull( date );
		return asset.isPublished() && ( asset.getPublicationDate() == null || asset.getPublicationDate().before( date ) );
	}

	/**
	 * Generate a unique object id for the given collection.  A collection is represented by an id
	 * (eg. wcm:asset:page) and a unique id with the collection id as prefix will be returned.
	 *
	 * @param collectionId the key should have
	 * @return unique key
	 */
	public static String generateObjectId( String collectionId ) {
		Assert.notNull( collectionId );
		return prefixObjectIdForCollection( UUID.randomUUID().toString(), collectionId );
	}

	/**
	 * Ensures a unique id is for a given collection.  If the object id starts with the collection id,
	 * it will be left unchanged, else the collection id will be prefixed.
	 *
	 * @param objectId     requested
	 * @param collectionId the key should have
	 * @return collection prefixed key
	 */
	public static String prefixObjectIdForCollection( String objectId, String collectionId ) {
		Assert.notNull( collectionId );
		Assert.notNull( objectId );
		return StringUtils.startsWith( objectId, collectionId + ":" ) ? objectId : collectionId + ":" + objectId;
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

	/**
	 * Create a full url by combining a base with a path segment.
	 * If the baseUrl contains a <strong>*</strong> marker, the path will be injected into the marker,
	 * else the path will simply be appended.
	 *
	 * @param baseUrl that may or may not contain a * marker
	 * @param path    to inject into the base url
	 * @return combined
	 */
	public static String combineUrlSegments( String baseUrl, String path ) {
		Assert.notNull( baseUrl );
		Assert.notNull( path );

		if ( StringUtils.contains( baseUrl, '*' ) ) {
			return StringUtils.replace( baseUrl, "*", path );
		}

		return StringUtils.removeEnd( baseUrl, "/" ) + "/" + path;
	}

	public static WebCmsDataAction convertImportActionToDataAction( Object existing, WebCmsDataImportAction requested ) {
		if ( existing != null ) {
			if ( requested == WebCmsDataImportAction.DELETE ) {
				return DELETE;
			}
			if ( requested == WebCmsDataImportAction.CREATE_OR_UPDATE || requested == WebCmsDataImportAction.UPDATE ) {
				return UPDATE;
			}
			if ( requested == WebCmsDataImportAction.CREATE_OR_REPLACE || requested == WebCmsDataImportAction.REPLACE ) {
				return REPLACE;
			}
		}
		else if ( requested == WebCmsDataImportAction.CREATE
				|| requested == WebCmsDataImportAction.CREATE_OR_UPDATE
				|| requested == WebCmsDataImportAction.CREATE_OR_REPLACE ) {
			return CREATE;
		}
		return null;
	}
}
