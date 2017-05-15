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

import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelHierarchy;
import com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateQueue;
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

	private static final String SCOPE_CONTAINER_CREATION = "_container_creation_scope";

	private static final String ATTR_COMPONENT = "component";
	private static final String ATTR_SCOPE = "scope";
	private static final String ATTR_SEARCH_PARENTS = "search-parent-scopes";
	private static final String ATTR_AUTO_CREATE = "auto-create";
	private static final String ATTR_TYPE = "type";
	private static final String ATTR_REPLACE = "always-replace";
	private static final String ATTR_PARSE_PLACEHOLDERS = "parse-placeholders";

	// used to indicate that all component attributes should in fact be ignored - set in the context of placeholders being parsed
	private static final String ATTR_NEVER_REPLACE = "never-replace";

	static final String MARKER_NEVER_REPLACE = PREFIX + ":" + ATTR_NEVER_REPLACE;

	private static final Collection<String> ATTRIBUTES_TO_REMOVE = Arrays.asList(
			ATTR_COMPONENT, ATTR_SCOPE, ATTR_SEARCH_PARENTS, ATTR_AUTO_CREATE, ATTR_TYPE, ATTR_REPLACE, ATTR_PARSE_PLACEHOLDERS, ATTR_NEVER_REPLACE
	);

	ComponentAttributesProcessor() {
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

			// during a parse placeholders phase, component attributes should be ignored and the template parsed for placeholders instead
			boolean ignoreComponent = elementTag.getAttribute( PREFIX, ATTR_NEVER_REPLACE ) != null;

			if ( !ignoreComponent ) {
				ApplicationContext applicationContext = RequestContextUtils.findWebApplicationContext( ( (WebEngineContext) context ).getRequest() );
				WebCmsComponentModelHierarchy components = applicationContext.getBean( WebCmsComponentModelHierarchy.class );

				String scopeName = elementTag.getAttributeValue( PREFIX, ATTR_SCOPE );
				WebCmsComponentModel component = fetchWebComponentModel( attributeValue, elementTag, components, scopeName );

				boolean parsePlaceholders = elementTag.getAttribute( PREFIX, ATTR_PARSE_PLACEHOLDERS ) != null;

				if ( component != null ) {
					elementTag = renderComponentModel( (IEngineContext) context, model, elementTag, modelFactory, component, parsePlaceholders );
				}
				else {
					WebCmsComponentAutoCreateQueue queue = applicationContext.getBean( WebCmsComponentAutoCreateQueue.class );
					val currentComponentInCreation = queue.getCurrentTask();

					String creationScope = currentComponentInCreation != null
							? currentComponentInCreation.getScopeName()
							: determineCreationScope( elementTag, components, scopeName );

					if ( creationScope != null ) {
						String componentType = elementTag.getAttributeValue( PREFIX, ATTR_TYPE );
						String taskId = queue.schedule( attributeValue, creationScope, componentType );

						if ( isStandaloneTag ) {
							renderEmptyBody( model, elementTag );
							model.add( modelFactory.createProcessingInstruction( START_INSTRUCTION, taskId ) );
							model.add( modelFactory.createProcessingInstruction( STOP_INSTRUCTION, taskId ) );
						}
						else {
							setContainerScopeForAllUnspecified( model, modelFactory );

							if ( parsePlaceholders ) {
								model.insert( 1, modelFactory.createProcessingInstruction( PlaceholderTemplatePostProcessor.START_PARSE_PLACEHOLDERS, "" ) );
							}

							model.insert( 1, modelFactory.createProcessingInstruction( START_INSTRUCTION, taskId ) );
							model.insert( model.size() - 1, modelFactory.createProcessingInstruction( STOP_INSTRUCTION, taskId ) );

							if ( currentComponentInCreation == null ) {
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
												"${@webCmsComponentAutoCreateQueue.getComponentCreated('" + taskId + "')}"
										)
								);
							}

							if ( parsePlaceholders ) {
								model.insert( model.size() - 1,
								              modelFactory.createProcessingInstruction( PlaceholderTemplatePostProcessor.STOP_PARSE_PLACEHOLDERS, "" ) );
							}
						}

					}
					else {
						boolean alwaysReplaceBody = elementTag.hasAttribute( PREFIX, ATTR_REPLACE );

						if ( alwaysReplaceBody ) {
							renderEmptyBody( model, elementTag );
						}
					}
				}
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
	                                                     boolean parsePlaceholders ) {
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

		String atrId = "_generatedComponentName" + COUNTER.incrementAndGet();
		context.setVariable( atrId, component );
		model.add( modelFactory.createStandaloneElementTag( "across:view", "element", "${" + atrId + "}" ) );

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
				if ( openElementTag.hasAttribute( PREFIX, ATTR_COMPONENT ) ) {
					model.replace( i, modelFactory.setAttribute( openElementTag, MARKER_NEVER_REPLACE, "true", AttributeValueQuotes.DOUBLE ) );
				}
			}
		}
	}

	private void setContainerScopeForAllUnspecified( IModel model, IModelFactory modelFactory ) {
		for ( int i = 1; i < model.size(); i++ ) {
			ITemplateEvent event = model.get( i );
			if ( event instanceof IOpenElementTag || event instanceof IStandaloneElementTag ) {
				IProcessableElementTag openElementTag = (IProcessableElementTag) event;
				if ( openElementTag.hasAttribute( PREFIX, ATTR_COMPONENT ) && !openElementTag.hasAttribute( PREFIX, ATTR_SCOPE ) ) {
					model.replace(
							i,
							modelFactory.setAttribute( openElementTag, PREFIX + ":" + ATTR_SCOPE, SCOPE_CONTAINER_CREATION, AttributeValueQuotes.DOUBLE )
					);
				}
			}
		}
	}

	private WebCmsComponentModel fetchWebComponentModel( String componentName,
	                                                     IProcessableElementTag elementTag,
	                                                     WebCmsComponentModelHierarchy components,
	                                                     String scopeName ) {
		if ( SCOPE_CONTAINER_CREATION.equals( scopeName ) ) {
			return null;
		}

		val searchParentsAttribute = elementTag.getAttribute( PREFIX, ATTR_SEARCH_PARENTS );
		boolean searchParentScopes = scopeName == null
				? searchParentsAttribute == null || !"false".equalsIgnoreCase( searchParentsAttribute.getValue() )
				: searchParentsAttribute != null && !"false".equalsIgnoreCase( searchParentsAttribute.getValue() );

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
