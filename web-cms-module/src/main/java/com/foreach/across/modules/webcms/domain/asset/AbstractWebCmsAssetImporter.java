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

import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import com.foreach.across.modules.webcms.data.WebCmsDataImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base class for importing a simple asset type.  An asset has a specific data key (in the asset collection)
 * and a class extending {@link WebCmsAsset}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
public abstract class AbstractWebCmsAssetImporter<T extends WebCmsAsset> implements WebCmsDataImporter
{
	protected final Logger LOG = LoggerFactory.getLogger( getClass() );

	private WebCmsAssetRepository assetRepository;
	private ConversionService conversionService;

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
	public final void importData( WebCmsDataEntry data ) {
		data.getData().forEach( ( key, properties ) -> importSingleAsset( new WebCmsDataEntry( key, data.getKey(), properties ) ) );
	}

	private void importSingleAsset( WebCmsDataEntry item ) {
		T existing = retrieveExistingAsset( (String) item.getData().get( "assetKey" ), item.getKey() );
		T dto = createDto( existing );

		if ( dto != null ) {
			LOG.trace( "{} WebCmsAsset {} with assetKey {}", dto.isNew() ? "Creating" : "Updating" );

			BeanWrapperImpl beanWrapper = new BeanWrapperImpl( dto );

			AtomicBoolean modified = new AtomicBoolean( false );

			item.getData().forEach( ( propertyName, propertyValue ) -> {
				TypeDescriptor typeDescriptor = beanWrapper.getPropertyTypeDescriptor( propertyName );
				Object valueToSet = conversionService.convert( propertyValue, TypeDescriptor.forObject( propertyValue ), typeDescriptor );
				Object currentValue = beanWrapper.getPropertyValue( propertyName );

				if ( dto.isNew() || !Objects.equals( currentValue, valueToSet ) ) {
					modified.set( true );
					beanWrapper.setPropertyValue( propertyName, valueToSet );
				}
			} );

			if ( modified.get() ) {
				T itemToSave = prepareForSaving( dto, item );

				if ( itemToSave != null ) {
					LOG.debug( "Saving WebCmsAsset {} with assetKey {} (insert: {}) - {}",
					           dataKey, itemToSave.getAssetKey(), dto.isNew(), dto );
					assetRepository.save( itemToSave );
				}
				else {
					LOG.trace( "Skipping WebCmsAsset {} import for assetKey {} - prepareForSaving returned null", dataKey, dto.getAssetKey() );
				}
			}
			else {
				LOG.trace( "Skipping WebCmsAsset {} import for assetKey {} - nothing modified", dataKey, dto.getAssetKey() );
			}
		}
		else {
			LOG.trace( "Skipping WebCmsAsset {} import for entry {} - no DTO was created", dataKey, item.getKey() );
		}
	}

	private T retrieveExistingAsset( String assetKey, String entryKey ) {
		WebCmsAsset existing = null;

		if ( assetKey != null ) {
			existing = assetRepository.findOneByAssetKey( assetKey );
		}

		return existing != null ? assetType.cast( existing ) : getExistingByEntryKey( entryKey );
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
	 * Create a DTO of the item to update.  If no item is expected to update, return a template for a new item.
	 *
	 * @param itemToUpdate or null if a new item should be created
	 * @return dto - not null
	 */
	protected abstract T createDto( T itemToUpdate );

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
	void setAssetRepository( WebCmsAssetRepository assetRepository ) {
		this.assetRepository = assetRepository;
	}

	@Autowired
	@Qualifier("webCmsDataConversionService")
	void setConversionService( ConversionService conversionService ) {
		this.conversionService = conversionService;
	}
}
