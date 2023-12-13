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
import com.foreach.across.core.development.AcrossDevelopmentMode;
import com.foreach.across.modules.entity.util.EntityUtils;
import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateTask.Attribute.*;

/**
 * Central API for automatic creation of {@link com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel}.
 * Usually triggered
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WebCmsComponentAutoCreateService
{
	private final AcrossDevelopmentMode developmentMode;
	private final WebCmsComponentModelService componentModelService;
	private final WebCmsDataConversionService dataConversionService;

	private Collection<WebCmsComponentAutoCreateStrategy> createStrategies = Collections.emptyList();

	/**
	 * The component type that should be used for all tasks where there is no explicit
	 * component type specified and there are no child tasks (not a container).
	 */
	@Setter
	private String defaultComponentType = "html";

	/**
	 * Attempts to resolve a requested component type by its type key.
	 * If none is specified, the default type will be returned.
	 * <p/>
	 * If a name is specified but no type can be found with that key, an {@link IllegalArgumentException} will be thrown.
	 *
	 * @param componentTypeName requested
	 * @return component type
	 */
	public WebCmsComponentType resolveComponentType( String componentTypeName ) {
		String nameToResolve = StringUtils.isEmpty( componentTypeName ) ? defaultComponentType : componentTypeName;
		return Optional.ofNullable( componentModelService.getComponentType( nameToResolve ) )
		               .orElseThrow( () -> new IllegalArgumentException( "Unknown component type: " + componentTypeName ) );
	}

	/**
	 * Create all components represented by a single task.
	 *
	 * @param task representing the components to create
	 * @return component model of the root component created
	 */
	@Transactional
	public synchronized WebCmsComponentModel createComponent( WebCmsComponentAutoCreateTask task ) {
		WebCmsComponentModel componentModel = buildComponent( task );

		if ( componentModel != null && componentModel.isNew() ) {
			WebCmsComponentModel existing
					= componentModelService.getComponentModelByNameAndDomain( componentModel.getName(), task.getOwner(), task.getDomain() );

			if ( existing == null ) {
				componentModelService.save( componentModel );
			}
			else {
				LOG.warn( "Skipping auto-creation of component, component with name {} already exists for owner {}", task.getComponentName(), task.getOwner() );
				componentModel = existing;
			}
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
		WebCmsComponentType componentType = task.getComponentType();

		if ( componentType != null ) {
			try {
				val component = componentModelService.createComponentModel( componentType, WebCmsComponentModel.class );
				component.setDomain( task.getDomain() );
				component.setName( task.getComponentName() );
				component.setTitle( EntityUtils.generateDisplayName( task.getComponentName() ) );
				component.setOwner( task.getOwner() );

				createStrategies.stream()
				                .filter( strategy -> strategy.supports( component, task ) )
				                .findFirst()
				                .orElseThrow( () -> new IllegalStateException( "No valid auto-create strategy for " + componentType ) )
				                .buildComponentModel( this, component, task );

				applyAttributeValues( component, task.getAttributeValues() );

				return component;
			}
			catch ( Exception e ) {
				LOG.error( "Unable to auto-create component, exception occurred", e );

				if ( developmentMode.isActive() ) {
					throw new WebCmsComponentAutoCreateException( "Unable to auto-create component " + task.getComponentName(), e );
				}
			}
		}
		else {
			LOG.error( "Unable to auto-create component, unknown component type: {}", task.getComponentType() );
		}

		return null;
	}

	private void applyAttributeValues( WebCmsComponentModel componentModel, List<WebCmsComponentAutoCreateTask.AttributeValue> attributeValues ) {
		BeanWrapperImpl component = new BeanWrapperImpl( componentModel );
		BeanWrapperImpl metadata = componentModel.hasMetadata() ? new BeanWrapperImpl( componentModel.getMetadata() ) : null;

		for ( WebCmsComponentAutoCreateTask.AttributeValue attributeValue : attributeValues ) {
			WebCmsComponentAutoCreateTask.Attribute attributeType = attributeValue.getAttribute();

			if ( attributeType == ANY || attributeType == PROPERTY ) {
				TypeDescriptor typeDescriptor = component.getPropertyTypeDescriptor( attributeValue.getKey() );
				if ( typeDescriptor != null ) {
					applyPropertyValue( component, attributeValue.getKey(), attributeValue.getValue(), typeDescriptor );
				}
				else if ( attributeType == ANY ) {
					attributeType = METADATA;
				}
				else {
					LOG.warn( "Ignoring property value for component {}: {}", componentModel.getName(), attributeValue );
					if ( developmentMode.isActive() ) {
						throw new WebCmsComponentAutoCreateException(
								"Not a valid property for component " + componentModel.getName() + ": " + attributeValue );
					}
				}
			}

			if ( attributeType == METADATA ) {
				TypeDescriptor typeDescriptor = metadata != null ? metadata.getPropertyTypeDescriptor( attributeValue.getKey() ) : null;
				if ( typeDescriptor != null ) {
					applyPropertyValue( metadata, attributeValue.getKey(), attributeValue.getValue(), typeDescriptor );
				}
				else {
					LOG.warn( "Ignoring property value for component metadata {}: {}", componentModel.getName(), attributeValue );
					if ( developmentMode.isActive() ) {
						throw new WebCmsComponentAutoCreateException(
								"Not a valid metadata property for component " + componentModel.getName() + ": " + attributeValue );
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void applyPropertyValue( BeanWrapper target, String propertyName, Object propertyValue, TypeDescriptor typeDescriptor ) {
		try {
			TypeDescriptor sourceType = TypeDescriptor.forObject( propertyValue );
			if ( propertyValue instanceof Map && !dataConversionService.canConvert( sourceType, typeDescriptor ) ) {
				Object currentPropertyValue = target.getPropertyValue( propertyName );

				if ( currentPropertyValue == null ) {
					throw new RuntimeException( "Unable to converted nested object - value is null for property " + propertyName );
				}

				dataConversionService.convertToPropertyValues( (Map<String, Object>) propertyValue, target );
			}
			else {
				Object valueToSet = dataConversionService.convert( propertyValue, sourceType, typeDescriptor );

				if ( propertyValue != null && valueToSet == null ) {
					throw new IllegalArgumentException(
							"Illegal converted value for '" + propertyName + "': " +
									sourceType.getName() + " to " + typeDescriptor.getName() + " resulted in null for '" + propertyValue + "'" );
				}

				target.setPropertyValue( propertyName, valueToSet );
			}
		}
		catch ( Exception e ) {
			LOG.error( "Unable to set property value on component", e );
			if ( developmentMode.isActive() ) {
				throw new WebCmsComponentAutoCreateException( "Unable to set component property value for " + propertyName + ": " + propertyValue, e );
			}
		}
	}

	@Autowired
	void setCreateStrategies( @RefreshableCollection(includeModuleInternals = true, incremental = true) Collection<WebCmsComponentAutoCreateStrategy> createStrategies ) {
		this.createStrategies = createStrategies;
	}

	static class WebCmsComponentAutoCreateException extends RuntimeException
	{
		public WebCmsComponentAutoCreateException( String message ) {
			super( message );
		}

		public WebCmsComponentAutoCreateException( String message, Throwable cause ) {
			super( message, cause );
		}
	}
}
