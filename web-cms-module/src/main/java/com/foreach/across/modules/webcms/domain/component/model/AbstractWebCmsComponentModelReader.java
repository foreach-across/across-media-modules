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
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.util.Objects;

/**
 * Base class that builds the metadata for a {@link WebCmsComponentModel} and supports component template.
 * A component type can have a <strong>componentTemplate</strong> component attached that will serve as the
 * base component for the type.  Note that the implemented {@link WebCmsComponentModel} type of the template
 * component must be the same as the type defined by the component type itself.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Slf4j
public abstract class AbstractWebCmsComponentModelReader<T extends WebCmsComponentModel> implements WebCmsComponentModelReader<T>
{
	private static final String TEMPLATE_COMPONENT = "componentTemplate";

	private WebCmsDataObjectMapper dataObjectMapper;
	private AutowireCapableBeanFactory beanFactory;

	private WebCmsComponentModelService componentModelService;

	@Override
	public final T readFromComponent( WebCmsComponent component ) {
		T model = createComponentModel( component );

		if ( model != null ) {
			model.setMetadata( buildMetadata( model.getMetadata(), component ) );
		}

		return model;
	}

	@SuppressWarnings("unchecked")
	private T createComponentModel( WebCmsComponent component ) {
		if ( component.isNew() ) {
			WebCmsDomain componentDomain = component.getDomain();
			WebCmsDomain componentTypeDomain = component.getComponentType().getDomain();

			WebCmsComponentModel template
					= componentModelService.getComponentModelByNameAndDomain( TEMPLATE_COMPONENT, component.getComponentType(), componentDomain );

			if ( template == null && !Objects.equals( componentDomain, componentTypeDomain ) ) {
				template = componentModelService.getComponentModelByNameAndDomain( TEMPLATE_COMPONENT, component.getComponentType(), componentTypeDomain );
			}

			if ( template != null ) {
				WebCmsComponentModel model = template.asComponentTemplate();
				model.setComponent( component );
				return (T) model;
			}
		}

		return buildComponentModel( component );
	}

	/**
	 * Override if you want to use a custom implementation for reading the metadata.
	 */
	protected Object buildMetadata( Object currentMetadata, WebCmsComponent component ) {
		boolean componentHasMetadata = !StringUtils.isEmpty( component.getMetadata() );
		String metadataClassName = component.getComponentType().getAttribute( WebCmsComponentModel.METADATA_CLASS_ATTRIBUTE );
		Object metadata = currentMetadata;

		if ( metadataClassName != null ) {
			try {
				Class<?> metadataType = Class.forName( metadataClassName, true, Thread.currentThread().getContextClassLoader() );
				if ( currentMetadata == null || !metadataType.isInstance( currentMetadata ) ) {
					metadata = beanFactory.createBean( metadataType );
				}
			}
			catch ( Exception e ) {
				LOG.error(
						"Exception creating metadata {} for component {}.  Possibly the class is not present, not public or does not have a parameter-less constructor?",
						metadataClassName, component, e );
			}
		}

		if ( componentHasMetadata ) {
			if ( !dataObjectMapper.updateFromString( component.getMetadata(), metadata, false ) ) {
				LOG.warn( "An exception occurred reading back metadata for component {}.  The actual metadata might not be complete: {}",
				          component, metadata );
			}
		}

		return metadata;
	}

	/**
	 * Build the basic {@link WebCmsComponentModel}. The return value should not have any metadata attached.
	 *
	 * @param component to read model for
	 * @return model
	 */
	protected abstract T buildComponentModel( WebCmsComponent component );

	@Autowired
	void setDataObjectMapper( WebCmsDataObjectMapper dataObjectMapper ) {
		this.dataObjectMapper = dataObjectMapper;
	}

	@Autowired
	void setBeanFactory( AutowireCapableBeanFactory beanFactory ) {
		this.beanFactory = beanFactory;
	}

	@Autowired
	void setComponentModelService( WebCmsComponentModelService componentModelService ) {
		this.componentModelService = componentModelService;
	}
}
