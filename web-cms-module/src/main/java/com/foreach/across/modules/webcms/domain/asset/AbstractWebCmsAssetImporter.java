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

package com.foreach.across.modules.webcms.domain.asset;

import com.foreach.across.modules.webcms.data.AbstractWebCmsDataImporter;
import com.foreach.across.modules.webcms.data.WebCmsDataAction;
import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for importing a simple asset type.  An asset has a specific data key (in the asset collection)
 * and a class extending {@link WebCmsAsset}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
public abstract class AbstractWebCmsAssetImporter<T extends WebCmsAsset> extends AbstractWebCmsDataImporter<T, T>
{
	private final String dataKey;
	private final Class<T> assetType;
	private WebCmsAssetRepository assetRepository;

	protected AbstractWebCmsAssetImporter( @NonNull String dataKey, @NonNull Class<T> assetType ) {
		this.dataKey = dataKey;
		this.assetType = assetType;
	}

	@Override
	public final boolean supports( WebCmsDataEntry data ) {
		return data.getParent() != null && "assets".equals( data.getParent().getParentKey() ) && dataKey.equals( data.getParentKey() );
	}

	@Override
	protected final T retrieveExistingInstance( WebCmsDataEntry item ) {
		String objectId = (String) item.getMapData().get( "objectId" );
		String entryKey = item.getKey();

		WebCmsAsset<?> existing = null;

		if ( objectId != null ) {
			validateObjectId( objectId );
			existing = assetRepository.findOneByObjectId( objectId ).orElse( null );
		}

		WebCmsDomain domain = retrieveDomainForDataEntry( item, assetType );

		return existing != null ? assetType.cast( existing ) : getExistingEntity( entryKey, item, domain );
	}

	/**
	 * Should validate the object id for the asset type requested.
	 * Will throw an exception if an object id is specified but it is not of valid form for the asset type.
	 */
	@SneakyThrows
	protected void validateObjectId( String objectId ) {
		T asset = assetType.newInstance();
		asset.setObjectId( objectId );

		if ( !StringUtils.equals( asset.getObjectId(), objectId ) ) {
			LOG.error( "Invalid objectId specified: {} would be converted to {}, only fully qualified object ids are supported", objectId,
			           asset.getObjectId() );
			throw new IllegalArgumentException(
					"Invalid objectId: " + objectId + " - only fully qualified object ids are supported, value would be converted to " + asset.getObjectId() );
		}
	}

	@Override
	protected void deleteInstance( T instance, WebCmsDataEntry data ) {
		assetRepository.delete( instance );
	}

	@Override
	protected void saveDto( T itemToSave, WebCmsDataAction action, WebCmsDataEntry data ) {
		LOG.debug( "Saving WebCmsAsset {} with objectId {} (insert: {}) - {}", dataKey, itemToSave.getObjectId(), itemToSave.isNew(), itemToSave );
		assetRepository.save( itemToSave );
	}

	/**
	 * If no asset has been found by the unique asset key and domain combination (or no asset key was defined), this method will be called with the entry key.
	 *
	 * @param entryKey  for the asset
	 * @param entryData for he asset
	 * @param domain    for the asset
	 * @return existing entity or null
	 */
	protected T getExistingEntity( String entryKey, WebCmsDataEntry entryData, WebCmsDomain domain ) {
		return null;
	}

	@Autowired
	public void setAssetRepository( WebCmsAssetRepository assetRepository ) {
		this.assetRepository = assetRepository;
	}
}
