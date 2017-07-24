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

import com.foreach.across.modules.webcms.data.json.WebCmsDataObjectMapper;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class with override-able methods for component model persistence.
 * Assumes the component model does not allow member components by default.
 *
 * @author Arne Vandamme
 * @see WebCmsComponentModelWriter
 * @since 0.0.2
 */
public abstract class AbstractWebCmsComponentModelWriter<T extends WebCmsComponentModel> implements WebCmsComponentModelWriter<T>
{
	private WebCmsComponentRepository componentRepository;
	private WebCmsDataObjectMapper dataObjectMapper;

	@Transactional
	@Override
	public final WebCmsComponent save( T componentModel ) {
		beforeUpdate( componentModel );

		WebCmsComponent mainComponent = componentModel.getComponent();
		buildMainComponent( componentModel, mainComponent );
		writeMetadata( componentModel, mainComponent );

		mainComponent = saveComponent( mainComponent );

		afterUpdate( componentModel, mainComponent );

		return mainComponent;
	}

	protected WebCmsComponent saveComponent( WebCmsComponent component ) {
		return componentRepository.save( component );
	}

	protected void beforeUpdate( T componentModel ) {
		removeOwnedComponents( componentModel );
	}

	/**
	 * Will remove all owned components if this component does not support any members.
	 *
	 * @param componentModel to update
	 */
	private void removeOwnedComponents( T componentModel ) {
		if ( !componentModel.isNew() ) {
			componentRepository.findAllByOwnerObjectIdOrderBySortIndexAsc( componentModel.getObjectId() )
			                   .forEach( componentRepository::delete );
		}
	}

	protected abstract void buildMainComponent( T componentModel, WebCmsComponent component );

	/**
	 * Serializes the metadata class using the {@link com.foreach.across.modules.webcms.data.json.WebCmsDataObjectMapper}.
	 * Override this method if you want to manually control how metadata gets written.
	 */
	protected void writeMetadata( T componentModel, WebCmsComponent component ) {
		component.setMetadata( componentModel.hasMetadata() ? dataObjectMapper.writeToString( componentModel.getMetadata() ) : null );
	}

	protected void afterUpdate( T componentModel, WebCmsComponent mainComponent ) {
	}

	@Autowired
	void setComponentRepository( WebCmsComponentRepository componentRepository ) {
		this.componentRepository = componentRepository;
	}

	@Autowired
	void setDataObjectMapper( WebCmsDataObjectMapper dataObjectMapper ) {
		this.dataObjectMapper = dataObjectMapper;
	}
}
