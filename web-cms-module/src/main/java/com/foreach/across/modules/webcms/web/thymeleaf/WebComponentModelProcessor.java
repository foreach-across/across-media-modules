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
package com.foreach.across.modules.webcms.web.thymeleaf;

import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentAutoCreateQueue;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelHierarchy;
import com.foreach.across.modules.webcms.domain.component.placeholder.WebCmsPlaceholderContentModel;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.thymeleaf.context.IEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.WebEngineContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.*;
import org.thymeleaf.processor.element.AbstractAttributeModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Arrays;
import java.util.Collection;

import static com.foreach.across.modules.webcms.web.thymeleaf.PlaceholderTemplateProcessor.DECREASE_PLACEHOLDER_LEVEL;
import static com.foreach.across.modules.webcms.web.thymeleaf.WebCmsDialect.PREFIX;
import static com.foreach.across.modules.webcms.web.thymeleaf.WebComponentModelTemplateProcessor.START_INSTRUCTION;
import static com.foreach.across.modules.webcms.web.thymeleaf.WebComponentModelTemplateProcessor.STOP_INSTRUCTION;

/**
 * Enables generic {@link com.foreach.across.modules.web.ui.ViewElement} rendering support.
 */
class WebComponentModelProcessor extends AbstractAttributeModelProcessor
{
	private static final String ATTR_COMPONENT = "component";
	private static final String ATTR_SCOPE = "scope";
	private static final String ATTR_SEARCH_PARENTS = "search-parent-scopes";
	private static final String ATTR_AUTO_CREATE = "auto-create";
	private static final String ATTR_TYPE = "type";
	private static final String ATTR_REPLACE = "always-replace";
	private static final String ATTR_PLACEHOLDERS = "placeholders";

	private static final Collection<String> ATTRIBUTES_TO_REMOVE = Arrays.asList(
			ATTR_COMPONENT, ATTR_SCOPE, ATTR_SEARCH_PARENTS, ATTR_AUTO_CREATE, ATTR_TYPE, ATTR_REPLACE, ATTR_PLACEHOLDERS
	);

	WebComponentModelProcessor() {
		super(
				TemplateMode.HTML,              // This processor will apply only to HTML mode
				PREFIX,        // Prefix to be applied to name for matching
				null,               // Tag name: match specifically this tag
				false,          // Apply dialect prefix to tag name
				ATTR_COMPONENT,              // No attribute name: will match by tag name
				true,          // No prefix to be applied to attribute name
				10000,
				false
		);
	}

	@Override
	protected void doProcess( ITemplateContext context,
	                          IModel model,
	                          AttributeName attributeName,
	                          String attributeValue,
	                          IElementModelStructureHandler structureHandler ) {
		if ( model.size() > 0 && model.get( 0 ) instanceof IProcessableElementTag ) {
			IProcessableElementTag elementTag = (IProcessableElementTag) model.get( 0 );
			boolean isStandaloneTag = elementTag instanceof IStandaloneElementTag;
			IModelFactory modelFactory = context.getModelFactory();

			ApplicationContext applicationContext = RequestContextUtils.findWebApplicationContext( ( (WebEngineContext) context ).getRequest() );
			WebCmsComponentModelHierarchy components = applicationContext.getBean( WebCmsComponentModelHierarchy.class );
			WebCmsPlaceholderContentModel placeholderContentModel = applicationContext.getBean( WebCmsPlaceholderContentModel.class );

			String scopeName = elementTag.getAttributeValue( PREFIX, ATTR_SCOPE );
			WebCmsComponentModel component = fetchWebComponentModel( attributeValue, elementTag, components, scopeName );

			boolean hasPlaceholders = elementTag.getAttribute( PREFIX, ATTR_PLACEHOLDERS ) != null;

			if ( hasPlaceholders ) {
				placeholderContentModel.increaseLevel();
			}

			if ( component != null ) {
				elementTag = renderComponentModel( (IEngineContext) context, model, elementTag, modelFactory, component, hasPlaceholders );
			}
			else {
				WebCmsComponentAutoCreateQueue queue = applicationContext.getBean( WebCmsComponentAutoCreateQueue.class );
				val task = queue.getCurrentTask();

				String creationScope = task != null ? task.getScopeName() : determineCreationScope( elementTag, components, scopeName );

				if ( creationScope != null ) {
					String componentType = elementTag.getAttributeValue( PREFIX, ATTR_TYPE );
					String componentId = queue.schedule( attributeValue, creationScope, componentType );

					if ( isStandaloneTag ) {
						renderEmptyBody( model, elementTag );
					}

					model.insert( 1, modelFactory.createProcessingInstruction( START_INSTRUCTION, componentId ) );
					model.insert( model.size() - 1, modelFactory.createProcessingInstruction( STOP_INSTRUCTION, componentId ) );
				}
				else {
					boolean alwaysReplaceBody = elementTag.hasAttribute( PREFIX, ATTR_REPLACE );

					if ( alwaysReplaceBody ) {
						renderEmptyBody( model, elementTag );
					}
				}
			}

			if ( hasPlaceholders ) {
				model.add( modelFactory.createProcessingInstruction( DECREASE_PLACEHOLDER_LEVEL, "" ) );
			}

			removeAttributes( model, elementTag, modelFactory );
		}
	}

	private void renderEmptyBody( IModel model, IProcessableElementTag elementTag ) {
		ITemplateEvent closeElementTag = null;
		if ( elementTag instanceof IOpenElementTag && model.size() > 1 ) {
			closeElementTag = model.get( model.size() - 1 );
		}
		model.reset();
		model.add( elementTag );
		if ( closeElementTag != null ) {
			model.add( closeElementTag );
		}
	}

	private String determineCreationScope( IProcessableElementTag elementTag, WebCmsComponentModelHierarchy components, String scopeName ) {
		String creationScope = null;
		IAttribute attribute = elementTag.getAttribute( PREFIX, ATTR_AUTO_CREATE );

		if ( attribute != null ) {
			creationScope = attribute.getValue();
			if ( creationScope == null ) {
				creationScope = scopeName;
			}
			if ( creationScope == null ) {
				creationScope = components.getDefaultScope();
			}
		}

		return creationScope;
	}

	private IProcessableElementTag renderComponentModel( IEngineContext context,
	                                                     IModel model,
	                                                     IProcessableElementTag elementTag,
	                                                     IModelFactory modelFactory,
	                                                     WebCmsComponentModel component,
	                                                     boolean hasPlaceholders ) {
		if ( hasPlaceholders ) {
			model.insert( 1, modelFactory.createProcessingInstruction( PlaceholderTemplateProcessor.START_INSTRUCTION, "" ) );
			model.replace( model.size() - 1, modelFactory.createProcessingInstruction( PlaceholderTemplateProcessor.STOP_INSTRUCTION, "" ) );
		}
		else {
			model.reset();

			if ( elementTag instanceof IOpenElementTag ) {
				model.add( elementTag );
			}
			else {
				model.add( modelFactory.createOpenElementTag(
						elementTag.getElementCompleteName(), elementTag.getAttributeMap(), AttributeValueQuotes.DOUBLE, false
				) );
				elementTag = (IProcessableElementTag) model.get( 0 );
			}
		}

		String atrId = "_generatedComponentName" + System.currentTimeMillis();
		context.setVariable( atrId, component );
		model.add( modelFactory.createStandaloneElementTag( "across:view", "element", "${" + atrId + "}" ) );
		model.add( modelFactory.createCloseElementTag( elementTag.getElementCompleteName() ) );
		return elementTag;
	}

	private WebCmsComponentModel fetchWebComponentModel( String componentName,
	                                                     IProcessableElementTag elementTag,
	                                                     WebCmsComponentModelHierarchy components,
	                                                     String scopeName ) {
		boolean searchParentScopes = !"false".equalsIgnoreCase( elementTag.getAttributeValue( PREFIX, ATTR_SEARCH_PARENTS ) );
		return scopeName != null
				? components.getFromScope( componentName, scopeName, searchParentScopes )
				: components.get( componentName, searchParentScopes );
	}

	private void removeAttributes( IModel model, IProcessableElementTag elementTag, IModelFactory modelFactory ) {
		IProcessableElementTag newFirstEvent = elementTag;

		for ( String attributeToRemove : ATTRIBUTES_TO_REMOVE ) {
			newFirstEvent = modelFactory.removeAttribute( newFirstEvent, PREFIX, attributeToRemove );
		}

		if ( newFirstEvent != elementTag ) {
			model.replace( 0, newFirstEvent );
		}
	}
}
