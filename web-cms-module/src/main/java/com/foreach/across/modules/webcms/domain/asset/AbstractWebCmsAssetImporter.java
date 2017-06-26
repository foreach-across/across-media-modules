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
import com.foreach.across.modules.webcms.data.WebCmsDataImportException;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * Base class for importing a simple asset type.  An asset has a specific data key (in the asset collection)
 * and a class extending {@link WebCmsAsset}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
public abstract class AbstractWebCmsAssetImporter<T extends WebCmsAsset> extends AbstractWebCmsDataImporter<T, T>
{
	protected final Logger LOG = LoggerFactory.getLogger( getClass() );

	private WebCmsAssetRepository assetRepository;

	private final String dataKey;
	private final Class<T> assetType;

	protected AbstractWebCmsAssetImporter( String dataKey, Class<T> assetType ) {
		Assert.notNull( assetType );
		Assert.notNull( dataKey );
		this.dataKey = dataKey;
		this.assetType = assetType;
	}

	@Override
	public final boolean supports( WebCmsDataEntry data ) {
		return "assets".equals( data.getParentKey() ) && dataKey.equals( data.getKey() );
	}

	@Override
	protected final T retrieveExistingInstance( WebCmsDataEntry item ) {
		String objectId = (String) item.getMapData().get( "objectId" );
		String entryKey = item.getKey();

		WebCmsAsset<?> existing = null;

		if ( objectId != null ) {
			validateObjectId( objectId );
			existing = assetRepository.findOneByObjectId( objectId );
		}

		return existing != null ? assetType.cast( existing ) : ( entryKey != null ? getExistingByEntryKey( entryKey ) : null );
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
			throw new WebCmsDataImportException(
					"Invalid objectId: " + objectId + " - only fully qualified object ids are supported, value would be converted to " + asset.getObjectId() );
		}
	}

	@Override
	protected void deleteInstance( T instance, WebCmsDataEntry data ) {
		assetRepository.delete( instance );
	}

	@Override
	protected void saveDto( T dto, WebCmsDataAction action, WebCmsDataEntry data ) {
		T itemToSave = prepareForSaving( dto, data );

		if ( itemToSave != null ) {
			LOG.debug( "Saving WebCmsAsset {} with objectId {} (insert: {}) - {}", dataKey, itemToSave.getObjectId(), dto.isNew(), dto );
			assetRepository.save( itemToSave );
		}
		else {
			LOG.trace( "Skipping WebCmsAsset {} import for objectId {} - prepareForSaving returned null", dataKey, dto.getObjectId() );
		}
	}

	/**
	 * Override if you want to post process an item before saving.
	 * Useful if you want to generate property values for example.
	 *
	 * @param itemToBeSaved original item to be saved
	 * @param data          that was used to build the item
	 * @return new item to be saved instead - null if saving should be skipped
	 */
	protected T prepareForSaving( T itemToBeSaved, WebCmsDataEntry data ) {
		return itemToBeSaved;
	}

	/**
	 * If no asset has been found by the unique asset key (or no asset key was defined), this method will be called with the entry key.
	 *
	 * @param entryKey for the asset
	 * @return existing entity or null
	 */
	protected T getExistingByEntryKey( String entryKey ) {
		return null;
	}

	@Autowired
	public void setAssetRepository( WebCmsAssetRepository assetRepository ) {
		this.assetRepository = assetRepository;
	}
}
