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

package com.foreach.across.modules.webcms.domain.component.model.create;

import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.across.modules.entity.util.EntityUtils;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentTypeRepository;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

/**
 * Central API for automatic creation of {@link com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel}.
 * Usually triggered
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WebCmsComponentAutoCreateService
{
	private final WebCmsComponentTypeRepository componentTypeRepository;
	private final WebCmsComponentModelService componentModelService;

	private Collection<WebCmsComponentAutoCreateStrategy> createStrategies = Collections.emptyList();

	/**
	 * The component type that should be used for all tasks where there is no explicit
	 * component type specified and there are no child tasks (not a container).
	 */
	@Setter
	private String defaultComponentType = "html";

	/**
	 * The component type that should be used for all tasks with child tasks (container)
	 * where there is no explicit component type specified.
	 */
	@Setter
	private String defaultContainerComponentType = "container";

	/**
	 * Create all components represented by a single task.
	 *
	 * @param task representing the components to create
	 * @return component model of the root component created
	 */
	@Transactional
	public WebCmsComponentModel createComponent( WebCmsComponentAutoCreateTask task ) {
		WebCmsComponentModel componentModel = buildComponent( task );

		if ( componentModel != null && componentModel.isNew() ) {
			componentModelService.save( componentModel );
		}

		return componentModel;
	}

	/**
	 * Builds the component model represented by a single task, but does not save it.
	 * Useful for strategy implementations that want to build child tasks.
	 *
	 * @param task representing the components to build
	 * @return component model of the root component created
	 */
	@SuppressWarnings("unchecked")
	public WebCmsComponentModel buildComponent( WebCmsComponentAutoCreateTask task ) {
		WebCmsComponentType componentType = determineComponentType( task );

		if ( componentType != null ) {
			try {
				val component = componentModelService.createComponentModel( componentType, WebCmsComponentModel.class );
				component.setName( task.getComponentName() );
				component.setTitle( EntityUtils.generateDisplayName( task.getComponentName() ) );
				component.setOwner( task.getOwner() );

				createStrategies.stream()
				                .filter( strategy -> strategy.supports( component, task ) )
				                .findFirst()
				                .orElseThrow( () -> new IllegalStateException( "No valid auto-create strategy for " + componentType ) )
				                .buildComponentModel( this, component, task );

				return component;
			}
			catch ( Exception e ) {
				LOG.error( "Unable to auto-create component, exception occurred", e );
			}
		}
		else {
			LOG.error( "Unable to auto-create component, unknown component type: {}", task.getComponentType() );
		}

		return null;
	}

	private WebCmsComponentType determineComponentType( WebCmsComponentAutoCreateTask task ) {
		String requested = task.getComponentType();

		if ( StringUtils.isEmpty( requested ) ) {
			requested = hasNonPlaceholderChildren( task ) ? defaultContainerComponentType : defaultComponentType;
		}
		return componentTypeRepository.findOneByTypeKey( requested );
	}

	private boolean hasNonPlaceholderChildren( WebCmsComponentAutoCreateTask task ) {
		return task.getChildren().stream().anyMatch( t -> !"placeholder".equals( t.getComponentType() ) );
	}

	@Autowired
	void setCreateStrategies( @RefreshableCollection(includeModuleInternals = true, incremental = true) Collection<WebCmsComponentAutoCreateStrategy> createStrategies ) {
		this.createStrategies = createStrategies;
	}
}
