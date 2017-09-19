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

package com.foreach.across.modules.webcms.domain.component;

import com.foreach.across.modules.entity.util.EntityUtils;
import com.foreach.across.modules.webcms.data.AbstractWebCmsDataImporter;
import com.foreach.across.modules.webcms.data.WebCmsDataAction;
import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainBound;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for importing a simple asset type.  An asset has a specific data key (in the asset collection)
 * and a class extending {@link WebCmsComponent}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@Scope("prototype")
public class WebCmsComponentImporter extends AbstractWebCmsDataImporter<WebCmsComponentModel, WebCmsComponentModel>
{
	private final String dataKey = "component";

	private WebCmsComponentRepository componentRepository;
	private WebCmsComponentModelService componentModelService;
	private WebCmsComponentValidator componentValidator;
	private WebCmsDataConversionService conversionService;
	private WebCmsObject owner;

	@Override
	public final boolean supports( WebCmsDataEntry data ) {
		return "assets".equals( data.getParent().getParentKey() ) && dataKey.equals( data.getParentKey() );
	}

	@Override
	protected WebCmsComponentModel retrieveExistingInstance( WebCmsDataEntry data ) {
		String objectId = (String) data.getMapData().get( "objectId" );
		String entryKey = StringUtils.defaultString( data.getKey(), (String) data.getMapData().get( "name" ) );

		WebCmsComponentModel existing = null;

		if ( objectId != null ) {
			existing = componentModelService.getComponentModel( objectId );
		}

		WebCmsDomain domain;

		if ( owner != null && ( owner instanceof WebCmsComponent || owner instanceof WebCmsComponentModel ) ) {
			domain = ( (WebCmsDomainBound) owner ).getDomain();
		}
		else {
			domain = retrieveDomainForDataEntry( data, WebCmsComponent.class );
		}

		if ( existing == null && entryKey != null ) {
			existing = componentModelService.getComponentModelByNameAndDomain( entryKey, WebCmsObject.forObjectId( getOwnerObjectId() ), domain );
		}

		return existing;
	}

	@Override
	protected WebCmsComponentModel createDto( WebCmsDataEntry data,
	                                          WebCmsComponentModel itemToUpdate,
	                                          WebCmsDataAction action,
	                                          Map<String, Object> dataValues ) {
		if ( itemToUpdate == null ) {
			WebCmsComponentModel componentModel = componentModelService.createComponentModel(
					conversionService.convert( dataValues.get( "componentType" ), WebCmsComponentType.class ),
					WebCmsComponentModel.class
			);
			componentModel.setName( data.getKey() );
			componentModel.setOwnerObjectId( getOwnerObjectId() );
			return componentModel;
		}

		String requestedComponentType = (String) dataValues.get( "componentType" );

		if ( ( requestedComponentType != null && !requestedComponentType.equals( itemToUpdate.getComponentType().getTypeKey() ) )
				|| WebCmsDataAction.REPLACE.equals( action ) ) {
			LOG.trace( "Changing component type: from {} to {} - resetting object", itemToUpdate.getComponentType().getTypeKey(), requestedComponentType );
			WebCmsComponentModel dto = componentModelService.createComponentModel( requestedComponentType, WebCmsComponentModel.class );
			WebCmsComponent newComponent = dto.getComponent();
			WebCmsComponent existing = itemToUpdate.getComponent();
			newComponent.setId( existing.getId() );
			newComponent.setOwnerObjectId( existing.getOwnerObjectId() );
			newComponent.setTitle( existing.getTitle() );
			newComponent.setName( existing.getName() );
			newComponent.setSortIndex( existing.getSortIndex() );
			newComponent.setCreatedBy( existing.getCreatedBy() );
			newComponent.setCreatedDate( existing.getCreatedDate() );
			newComponent.setDomain( existing.getDomain() );
			addForceUpdateProperty( dataValues );
			return dto;
		}

		return itemToUpdate;
	}

	@Override
	protected void deleteInstance( WebCmsComponentModel instance, WebCmsDataEntry data ) {
		LOG.trace( "WebCmsComponent {} with objectId {}: removing component", dataKey, instance.getObjectId() );
		componentRepository.delete( instance.getComponent() );
	}

	/**
	 * Override if you want to post process an item before saving.
	 * Useful if you want to generate property values for example.
	 *
	 * @param itemToBeSaved original item to be saved
	 * @param data          that was used to build the item
	 * @return new item to be saved instead - null if saving should be skipped
	 */
	protected WebCmsComponentModel prepareForSaving( WebCmsComponentModel itemToBeSaved, WebCmsDataEntry data ) {
		if ( itemToBeSaved.getName() == null ) {
			itemToBeSaved.setName( data.getKey() );
		}
		if ( !StringUtils.isBlank( itemToBeSaved.getName() ) && StringUtils.isEmpty( itemToBeSaved.getComponent().getTitle() ) ) {
			itemToBeSaved.setTitle( EntityUtils.generateDisplayName( itemToBeSaved.getName() ) );
		}
		return itemToBeSaved;
	}

	@SuppressWarnings("findbugs:RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
	@Override
	protected void saveDto( WebCmsComponentModel itemToSave, WebCmsDataAction action, WebCmsDataEntry data ) {
		LOG.debug( "Saving WebCmsComponent {} with objectId {} (insert: {}) - {}",
		           dataKey, itemToSave.getObjectId(), itemToSave.isNew(), itemToSave );
		componentModelService.save( itemToSave );
	}

	@Override
	protected boolean applyDataValues( Map<String, Object> values, WebCmsComponentModel dto ) {
		Map<String, Object> mapData = new HashMap<>( values );
		mapData.remove( "componentType" );

		return super.applyDataValues( mapData, dto );
	}

	private String getOwnerObjectId() {
		return owner != null ? owner.getObjectId() : null;
	}

	/**
	 * Set the owner of the objects being created.
	 */
	public void setOwner( WebCmsObject owner ) {
		this.owner = owner;
	}

	@Override
	protected void validate( WebCmsComponentModel itemToBeSaved, Errors errors ) {
		componentValidator.validate( itemToBeSaved.getComponent(), errors );
	}

	@Autowired
	void setComponentModelService( WebCmsComponentModelService componentModelService ) {
		this.componentModelService = componentModelService;
	}

	@Autowired
	void setComponentRepository( WebCmsComponentRepository componentRepository ) {
		this.componentRepository = componentRepository;
	}

	@Autowired
	void setComponentValidator( WebCmsComponentValidator componentValidator ) {
		this.componentValidator = componentValidator;
	}

	@Autowired
	void setConversionService( WebCmsDataConversionService conversionService ) {
		this.conversionService = conversionService;
	}
}
