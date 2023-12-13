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

package com.foreach.across.modules.webcms.domain.component.model;

import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.component.*;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
final class WebCmsComponentModelServiceImpl implements WebCmsComponentModelService
{
	private final WebCmsComponentRepository componentRepository;
	private final WebCmsMultiDomainService multiDomainService;
	private final WebCmsTypeSpecifierService typeSpecifierService;

	private Collection<WebCmsComponentModelReader> modelReaders = Collections.emptyList();
	private Collection<WebCmsComponentModelWriter> modelWriters = Collections.emptyList();

	@Override
	public WebCmsComponentType getComponentType( String componentTypeKey ) {
		return typeSpecifierService.getTypeSpecifierByKey( componentTypeKey, WebCmsComponentType.class );
	}

	@Override
	public WebCmsComponentType getComponentType( String componentTypeKey, WebCmsDomain domain ) {
		return typeSpecifierService.getTypeSpecifierByKey( componentTypeKey, WebCmsComponentType.class, domain );
	}

	@Override
	public <U extends WebCmsComponentModel> U createComponentModel( String componentTypeKey, Class<U> expectedType ) {
		Assert.notNull( componentTypeKey, "componentTypeKey is required" );
		return createComponentModel( getComponentType( componentTypeKey ), expectedType );
	}

	@Override
	public <U extends WebCmsComponentModel> U createComponentModel( String componentTypeKey, WebCmsDomain domain, Class<U> expectedType ) {
		return createComponentModel( getComponentType( componentTypeKey, domain ), expectedType );
	}

	@Override
	public <U extends WebCmsComponentModel> U createComponentModel( WebCmsComponentType componentType, Class<U> expectedType ) {
		Assert.notNull( componentType, "componentType is required" );
		Assert.notNull( expectedType, "expectedType is required" );

		WebCmsComponent component = new WebCmsComponent();
		component.setComponentType( componentType );
		component.setDomain( multiDomainService.getCurrentDomainForType( WebCmsComponent.class ) );
		return expectedType.cast( buildModelForComponent( component ) );
	}

	@Override
	public <U extends WebCmsComponentModel> U getComponentModel( String objectId, Class<U> expectedType ) {
		Assert.notNull( expectedType, "expectedType is required" );
		return expectedType.cast( getComponentModel( objectId ) );
	}

	@Override
	public WebCmsComponentModel getComponentModel( String objectId ) {
		Assert.notNull( objectId, "objectId is required" );
		WebCmsComponent component = componentRepository.findOneByObjectId( objectId ).orElse( null );
		return component != null ? buildModelForComponent( component ) : null;
	}

	@Override
	public <U extends WebCmsComponentModel> U getComponentModelByName( String componentName, WebCmsObject owner, Class<U> expectedType ) {
		Assert.notNull( expectedType, "expectedType is required" );
		return expectedType.cast( getComponentModelByName( componentName, owner ) );
	}

	@Override
	public WebCmsComponentModel getComponentModelByName( String componentName, WebCmsObject owner ) {
		return getComponentModelByNameAndDomain( componentName, owner, multiDomainService.getCurrentDomainForType( WebCmsComponent.class ) );
	}

	@Override
	public <U extends WebCmsComponentModel> U getComponentModelByNameAndDomain( String componentName,
	                                                                            WebCmsObject owner,
	                                                                            WebCmsDomain domain,
	                                                                            Class<U> expectedType ) {
		Assert.notNull( expectedType, "expectedType is required" );
		return expectedType.cast( getComponentModelByNameAndDomain( componentName, owner, domain ) );
	}

	@Override
	public WebCmsComponentModel getComponentModelByNameAndDomain( String componentName, WebCmsObject owner, WebCmsDomain domain ) {
		Assert.notNull( componentName, "componentName is required" );
		return componentRepository.findOneByOwnerObjectIdAndNameAndDomain( owner != null ? owner.getObjectId() : null, componentName, domain )
		                          .map( this::buildModelForComponent )
		                          .orElse( null );
	}

	@Override
	public WebCmsComponentModelSet buildComponentModelSetForOwner( WebCmsObject object, boolean eager ) {
		return buildComponentModelSetForOwner( object, multiDomainService.getCurrentDomainForType( WebCmsComponent.class ), eager );
	}

	@Override
	public WebCmsComponentModelSet buildComponentModelSetForOwner( WebCmsObject object, WebCmsDomain domain, boolean eager ) {
		Assert.notNull( object, "owner is required" );

		WebCmsComponentModelSet modelSet = new WebCmsComponentModelSet();
		modelSet.setOwner( object );
		modelSet.setDomain( domain );

		if ( eager ) {
			componentRepository.findAllByOwnerObjectIdAndDomainOrderBySortIndexAsc( object.getObjectId(), domain )
			                   .stream()
			                   .filter( c -> !StringUtils.isEmpty( c.getName() ) )
			                   .forEach( component -> modelSet.add( buildModelForComponent( component ) ) );
		}
		else {
			modelSet.setFetcherFunction( ( owner, componentName ) -> this.getComponentModelByNameAndDomain( componentName, owner, domain ) );
		}

		return modelSet;
	}

	@Override
	public Collection<WebCmsComponentModel> getComponentModelsForOwner( WebCmsObject object ) {
		return getComponentModelsForOwner( object, multiDomainService.getCurrentDomainForType( WebCmsComponent.class ) );
	}

	@Override
	public Collection<WebCmsComponentModel> getComponentModelsForOwner( WebCmsObject object, WebCmsDomain domain ) {
		return componentRepository.findAllByOwnerObjectIdAndDomainOrderBySortIndexAsc( object.getObjectId(), domain )
		                          .stream()
		                          .map( this::buildModelForComponent )
		                          .collect( Collectors.toList() );
	}

	@Override
	public WebCmsComponentModel buildModelForComponent( WebCmsComponent component ) {
		return buildModelForComponent( component, WebCmsComponentModel.class );
	}

	@Override
	public <U extends WebCmsComponentModel> U buildModelForComponent( WebCmsComponent component, Class<U> expectedType ) {
		Assert.notNull( component, "component is required" );
		return expectedType.cast(
				modelReaders.stream()
				            .filter( r -> r.supports( component ) )
				            .findFirst()
				            .orElseThrow( () -> new UnknownWebCmsComponentException( component ) )
				            .readFromComponent( component )
		);
	}

	@Transactional
	@Override
	public WebCmsComponent save( WebCmsComponent component ) {
		if ( component.isNew() ) {
			return save( buildModelForComponent( component ) );
		}

		return componentRepository.save( component );
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public WebCmsComponent save( WebCmsComponentModel componentModel ) {
		return modelWriters.stream()
		                   .filter( r -> r.supports( componentModel ) )
		                   .findFirst()
		                   .orElseThrow( () -> new UnknownWebCmsComponentModelException( componentModel ) )
		                   .save( componentModel );
	}

	@Autowired
	void setModelReaders( @RefreshableCollection(includeModuleInternals = true, incremental = true) Collection<WebCmsComponentModelReader> modelReaders ) {
		this.modelReaders = modelReaders;
	}

	@Autowired
	void setModelWriters( @RefreshableCollection(includeModuleInternals = true, incremental = true) Collection<WebCmsComponentModelWriter> modelWriters ) {
		this.modelWriters = modelWriters;
	}
}
