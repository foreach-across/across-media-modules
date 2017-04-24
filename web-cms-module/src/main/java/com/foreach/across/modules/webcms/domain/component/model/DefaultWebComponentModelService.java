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
import com.foreach.across.modules.webcms.domain.component.UnknownWebCmsComponentException;
import com.foreach.across.modules.webcms.domain.component.UnknownWebComponentModelException;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class DefaultWebComponentModelService implements WebComponentModelService
{
	private final WebCmsComponentRepository componentRepository;

	private Collection<WebComponentModelReader> modelReaders = Collections.emptyList();
	private Collection<WebComponentModelWriter> modelWriters = Collections.emptyList();

	@Override
	public WebComponentModel getWebComponent( String componentName, WebCmsObject owner ) {
		return Optional.ofNullable( componentRepository.findOneByOwnerObjectIdAndName( owner != null ? owner.getObjectId() : null, componentName ) )
		               .map( this::readFromComponent )
		               .orElse( null );
	}

	@Override
	public OrderedWebComponentModelSet getWebComponentsForOwner( WebCmsObject object ) {
		Assert.notNull( object );

		OrderedWebComponentModelSet modelSet = new OrderedWebComponentModelSet();
		modelSet.setOwner( object );
		componentRepository.findAllByOwnerObjectIdOrderBySortIndexAsc( object.getObjectId() )
		                   .forEach( component -> modelSet.add( readFromComponent( component ) ) );

		return modelSet;
	}

	@Override
	public WebComponentModel readFromComponent( WebCmsComponent component ) {
		return modelReaders.stream()
		                   .filter( r -> r.supports( component ) )
		                   .findFirst()
		                   .orElseThrow( () -> new UnknownWebCmsComponentException( component ) )
		                   .readFromComponent( component );
	}

	@SuppressWarnings("unchecked")
	@Override
	public void writeToComponent( WebComponentModel componentModel, WebCmsComponent component ) {
		modelWriters.stream()
		            .filter( r -> r.supports( componentModel ) )
		            .findFirst()
		            .orElseThrow( () -> new UnknownWebComponentModelException( componentModel ) )
		            .writeToComponent( componentModel, component );
	}

	@Autowired
	void setModelReaders( @RefreshableCollection(includeModuleInternals = true) Collection<WebComponentModelReader> modelReaders ) {
		this.modelReaders = modelReaders;
	}

	@Autowired
	void setModelWriters( @RefreshableCollection(includeModuleInternals = true) Collection<WebComponentModelWriter> modelWriters ) {
		this.modelWriters = modelWriters;
	}
}
