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

import com.foreach.across.modules.webcms.domain.component.WebCmsComponentUtils;
import com.foreach.across.modules.webcms.domain.component.WebCmsContentMarker;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelHierarchy;
import com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateQueue;
import com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateTask;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
import java.util.concurrent.atomic.AtomicInteger;

import static com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateQueue.CONTAINER_MEMBER_SCOPE;
import static com.foreach.across.modules.webcms.web.thymeleaf.ComponentTemplatePostProcessor.START_INSTRUCTION;
import static com.foreach.across.modules.webcms.web.thymeleaf.ComponentTemplatePostProcessor.STOP_INSTRUCTION;
import static com.foreach.across.modules.webcms.web.thymeleaf.WebCmsDialect.PREFIX;

/**
 * Enables generic {@link com.foreach.across.modules.web.ui.ViewElement} rendering support.
 * Uses processing instructions to signal how output handling should occur with regards to placeholders and auto-creation.
 *
 * @see PlaceholderAttributeProcessor
 * @see ComponentTemplatePostProcessor
 * @see PlaceholderTemplatePostProcessor
 * @see 0.0.2
 */
final class ComponentAttributesProcessor extends AbstractAttributeModelProcessor
{
	private static final AtomicInteger COUNTER = new AtomicInteger();

	static final String ATTR_COMPONENT = PREFIX + ":component";
	static final String ATTR_AUTO_CREATE = PREFIX + ":auto-create";
	static final String ATTR_SCOPE = PREFIX + ":scope";
	static final String ATTR_SEARCH_PARENTS = PREFIX + ":search-parent-scopes";
	static final String ATTR_TYPE = PREFIX + ":type";
	static final String ATTR_ALWAYS_REPLACE = PREFIX + ":always-replace";
	static final String ATTR_PARSE_PLACEHOLDERS = PREFIX + ":parse-placeholders";
	static final String ATTR_PLACEHOLDER = PREFIX + ":placeholder";
	static final String ATTR_PARENT_CREATE_INCLUDE = PREFIX + ":parent-create-include";
	static final String ATTR_PLACEHOLDER_AS_PARENT = PREFIX + ":auto-create-paceholder-parent";

	// used to indicate that all component attributes should in fact be ignored - set in the context of placeholders being parsed
	static final String ATTR_NEVER_REPLACE = PREFIX + ":never-replace";

	private static final Collection<String> ATTRIBUTES_TO_REMOVE = Arrays.asList(
			ATTR_COMPONENT, ATTR_SCOPE, ATTR_SEARCH_PARENTS, ATTR_AUTO_CREATE, ATTR_TYPE, ATTR_ALWAYS_REPLACE, ATTR_PARSE_PLACEHOLDERS, ATTR_NEVER_REPLACE,
			ATTR_PARENT_CREATE_INCLUDE, ATTR_PLACEHOLDER_AS_PARENT
	);

	ComponentAttributesProcessor() {
		super(
				TemplateMode.HTML,  // This processor will apply only to HTML mode
				PREFIX,             // Prefix to be applied to name for matching
				null,               // Tag name: match specifically this tag
				false,              // Apply dialect prefix to tag name
				"component",        // attribute name
				true,               // Apply prefix to attribute name
				100,
				false
		);
	}

	@Override
	protected void doProcess( ITemplateContext templateContext,
	                          IModel model,
	                          AttributeName attributeName,
	                          String attributeValue,
	                          IElementModelStructureHandler structureHandler ) {
		if ( model.size() > 0 && model.get( 0 ) instanceof IProcessableElementTag ) {
			WebEngineContext context = (WebEngineContext) templateContext;

			IProcessableElementTag elementTag = (IProcessableElementTag) model.get( 0 );
			boolean isStandaloneTag = elementTag instanceof IStandaloneElementTag;
			IModelFactory modelFactory = context.getModelFactory();

			// during a parse placeholders phase, component attributes should be ignored and the template parsed for placeholders instead
			boolean ignoreComponent = elementTag.hasAttribute( ATTR_NEVER_REPLACE );

			if ( !ignoreComponent ) {
				ApplicationContext applicationContext = RequestContextUtils.findWebApplicationContext( ( context ).getRequest() );
				WebCmsComponentModelHierarchy components = applicationContext.getBean( WebCmsComponentModelHierarchy.class );

				String scopeName = elementTag.getAttributeValue( ATTR_SCOPE );
				ComponentModelLookup lookupResult = lookupWebComponentModel( attributeValue, elementTag, components, scopeName );

				WebCmsComponentAutoCreateQueue creationQueue = applicationContext.getBean( WebCmsComponentAutoCreateQueue.class );
				WebCmsComponentAutoCreateTask currentCreationTask = creationQueue.getCurrentTask();

				boolean parsePlaceholders = elementTag.getAttribute( ATTR_PARSE_PLACEHOLDERS ) != null;
				boolean shouldRenderComponentOutput = currentCreationTask == null
						|| elementTag.hasAttribute( ATTR_PARENT_CREATE_INCLUDE );

				if ( lookupResult != null && lookupResult.hasComponentModel() ) {
					elementTag = renderComponentModel(
							context, model, elementTag, modelFactory, lookupResult.getComponentModel(), parsePlaceholders, shouldRenderComponentOutput
					);
				}
				else {
					String creationScope = determineCreationScope( elementTag, components, scopeName );

					if ( creationScope != null && !"_placeholder".equals( creationScope ) ) {
						autoCreateComponent(
								model, attributeValue, elementTag, isStandaloneTag, modelFactory, parsePlaceholders, creationQueue, currentCreationTask,
								creationScope, shouldRenderComponentOutput
						);

					}
					else if ( elementTag.hasAttribute( ATTR_ALWAYS_REPLACE ) || !shouldRenderComponentOutput ) {
						renderEmptyBody( model, elementTag );
					}
				}

				if ( !shouldRenderComponentOutput ) {
					writeComponentContentMarker( model, modelFactory, lookupResult );
				}
			}

			removeProcessedAttributesFromTag( model, elementTag, modelFactory );
		}
	}

	private void writeComponentContentMarker( IModel model, IModelFactory modelFactory, ComponentModelLookup lookupResult ) {
		if ( lookupResult != null ) {
			model.insert( 1, modelFactory.createProcessingInstruction( ComponentTemplatePostProcessor.COMPONENT_RENDER, lookupResult.toString() ) );
		}
	}

	private void autoCreateComponent( IModel model,
	                                  String attributeValue,
	                                  IProcessableElementTag elementTag,
	                                  boolean isStandaloneTag,
	                                  IModelFactory modelFactory,
	                                  boolean parsePlaceholders,
	                                  WebCmsComponentAutoCreateQueue queue,
	                                  WebCmsComponentAutoCreateTask currentComponentInCreation,
	                                  String creationScope,
	                                  boolean shouldRenderComponent ) {
		String componentType = elementTag.getAttributeValue( ATTR_TYPE );
		WebCmsComponentAutoCreateTask task = queue.schedule( attributeValue, creationScope, componentType );

		boolean createContainerMembers = WebCmsComponentUtils.isContainerType( task.getComponentType() );

		// should also auto-create container members?
		if ( isStandaloneTag ) {
			renderEmptyBody( model, elementTag );
			model.add( modelFactory.createProcessingInstruction( START_INSTRUCTION, task.getTaskId() ) );
			model.add( modelFactory.createProcessingInstruction( STOP_INSTRUCTION, task.getTaskId() ) );
		}
		else {
			if ( createContainerMembers ) {
				setContainerScopeForAllUnspecified( model, modelFactory );
			}

			if ( parsePlaceholders ) {
				model.insert( 1, modelFactory.createProcessingInstruction( PlaceholderTemplatePostProcessor.START_PARSE_PLACEHOLDERS, "" ) );
			}

			model.insert( 1, modelFactory.createProcessingInstruction( START_INSTRUCTION, task.getTaskId() ) );
			model.insert( model.size() - 1, modelFactory.createProcessingInstruction( STOP_INSTRUCTION, task.getTaskId() ) );

			if ( shouldRenderComponent ) {
				model.insert( 1,
				              modelFactory.createProcessingInstruction( PlaceholderTemplatePostProcessor.START_IGNORE_NON_PLACEHOLDERS, "" ) );
				model.insert(
						model.size() - 1,
						modelFactory.createProcessingInstruction( PlaceholderTemplatePostProcessor.STOP_IGNORE_NON_PLACEHOLDERS, "" )
				);

				model.insert(
						model.size() - 1,
						modelFactory.createStandaloneElementTag(
								"across:view",
								"element",
								"${@webCmsComponentAutoCreateQueue.getComponentCreated('" + task.getTaskId() + "')}"
						)
				);
			}

			if ( parsePlaceholders ) {
				model.insert( model.size() - 1,
				              modelFactory.createProcessingInstruction( PlaceholderTemplatePostProcessor.STOP_PARSE_PLACEHOLDERS, "" ) );
			}
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
		IAttribute attribute = elementTag.getAttribute( ATTR_AUTO_CREATE );

		if ( attribute != null ) {
			creationScope = attribute.getValue();
			if ( creationScope == null ) {
				creationScope = scopeName;
			}
			if ( creationScope == null ) {
				creationScope = components.getDefaultScope();
			}
		}
		else if ( CONTAINER_MEMBER_SCOPE.equals( scopeName ) ) {
			creationScope = CONTAINER_MEMBER_SCOPE;
		}

		return creationScope;
	}

	private IProcessableElementTag renderComponentModel( IEngineContext context,
	                                                     IModel model,
	                                                     IProcessableElementTag elementTag,
	                                                     IModelFactory modelFactory,
	                                                     WebCmsComponentModel component,
	                                                     boolean parsePlaceholders,
	                                                     boolean renderComponentOutput ) {
		if ( parsePlaceholders ) {
			model.insert( 1, modelFactory.createProcessingInstruction( PlaceholderTemplatePostProcessor.START_IGNORE_NON_PLACEHOLDERS, "" ) );
			model.insert( 1, modelFactory.createProcessingInstruction( PlaceholderTemplatePostProcessor.START_PARSE_PLACEHOLDERS, "" ) );

			if ( model.get( model.size() - 1 ) instanceof ICloseElementTag ) {
				model.remove( model.size() - 1 );
			}

			disableComponentBlocksInsidePlaceholderParsing( model, modelFactory );

			model.add( modelFactory.createProcessingInstruction( PlaceholderTemplatePostProcessor.STOP_IGNORE_NON_PLACEHOLDERS, "" ) );
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

		if ( renderComponentOutput ) {
			String atrId = "_generatedComponentName" + COUNTER.incrementAndGet();
			context.setVariable( atrId, component );
			model.add( modelFactory.createStandaloneElementTag( "across:view", "element", "${" + atrId + "}" ) );

		}

		if ( parsePlaceholders ) {
			// only decrease placeholder level after the actual component has been rendered
			model.add( modelFactory.createProcessingInstruction( PlaceholderTemplatePostProcessor.STOP_PARSE_PLACEHOLDERS, "" ) );
		}

		model.add( modelFactory.createCloseElementTag( elementTag.getElementCompleteName() ) );

		return elementTag;
	}

	private void disableComponentBlocksInsidePlaceholderParsing( IModel model, IModelFactory modelFactory ) {
		for ( int i = 2; i < model.size(); i++ ) {
			ITemplateEvent event = model.get( i );
			if ( event instanceof IOpenElementTag || event instanceof IStandaloneElementTag ) {
				IProcessableElementTag openElementTag = (IProcessableElementTag) event;
				if ( openElementTag.hasAttribute( ATTR_COMPONENT ) ) {
					model.replace( i, modelFactory.setAttribute( openElementTag, ATTR_NEVER_REPLACE, "true", AttributeValueQuotes.DOUBLE ) );
				}
				// not in auto creation, ensure placeholders and components are not included in the markup itself
				if ( openElementTag.hasAttribute( ATTR_PARENT_CREATE_INCLUDE ) ) {
					model.replace( i, modelFactory.removeAttribute( openElementTag, ATTR_PARENT_CREATE_INCLUDE ) );
				}
			}
		}
	}

	private void setContainerScopeForAllUnspecified( IModel model, IModelFactory modelFactory ) {
		for ( int i = 1; i < model.size(); i++ ) {
			ITemplateEvent event = model.get( i );
			if ( event instanceof IOpenElementTag || event instanceof IStandaloneElementTag ) {
				IProcessableElementTag original = (IProcessableElementTag) event;
				IProcessableElementTag openElementTag = original;
				if ( openElementTag.hasAttribute( ATTR_COMPONENT ) ) {
					if ( !openElementTag.hasAttribute( ATTR_SCOPE ) ) {
						openElementTag = modelFactory.setAttribute(
								openElementTag, ATTR_SCOPE, CONTAINER_MEMBER_SCOPE, AttributeValueQuotes.DOUBLE
						);
					}
					if ( "_placeholder".equals( openElementTag.getAttributeValue( ATTR_AUTO_CREATE ) ) ) {
						openElementTag = modelFactory.removeAttribute( openElementTag, ATTR_AUTO_CREATE );
					}
					if ( original != openElementTag ) {
						model.replace( i, openElementTag );
					}
				}
			}
		}
	}

	private ComponentModelLookup lookupWebComponentModel( String componentName,
	                                                      IProcessableElementTag elementTag,
	                                                      WebCmsComponentModelHierarchy components,
	                                                      String scopeName ) {
		if ( CONTAINER_MEMBER_SCOPE.equals( scopeName ) ) {
			return null;
		}

		val searchParentsAttribute = elementTag.getAttribute( ATTR_SEARCH_PARENTS );
		boolean searchParentScopes = scopeName == null
				? searchParentsAttribute == null || !"false".equalsIgnoreCase( searchParentsAttribute.getValue() )
				: searchParentsAttribute != null && !"false".equalsIgnoreCase( searchParentsAttribute.getValue() );

		return new ComponentModelLookup(
				componentName,
				scopeName != null ? scopeName : "default",
				searchParentScopes,
				scopeName != null
						? components.getFromScope( componentName, scopeName, searchParentScopes )
						: components.get( componentName, searchParentScopes )
		);
	}

	private void removeProcessedAttributesFromTag( IModel model, IProcessableElementTag elementTag, IModelFactory modelFactory ) {
		IProcessableElementTag newFirstEvent = elementTag;

		for ( String attributeToRemove : ATTRIBUTES_TO_REMOVE ) {
			newFirstEvent = modelFactory.removeAttribute( newFirstEvent, attributeToRemove );
		}

		if ( newFirstEvent != elementTag ) {
			model.replace( 0, newFirstEvent );
		}
	}

	@Getter
	@Setter
	@RequiredArgsConstructor
	private static class ComponentModelLookup
	{
		private final String componentName;
		private final String scopeName;
		private final boolean searchParents;
		private final WebCmsComponentModel componentModel;

		boolean hasComponentModel() {
			return componentModel != null;
		}

		@Override
		public String toString() {
			return new WebCmsContentMarker(
					"wcm:component",
					componentName + "," + scopeName + "," + searchParents
			).toString();
		}
	}
}
