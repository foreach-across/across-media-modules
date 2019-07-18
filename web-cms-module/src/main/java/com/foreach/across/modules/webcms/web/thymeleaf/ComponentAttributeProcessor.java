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
import com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateTask.Attribute;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.IEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.*;
import org.thymeleaf.processor.element.AbstractAttributeModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateQueue.CONTAINER_MEMBER_SCOPE;
import static com.foreach.across.modules.webcms.web.thymeleaf.ComponentTemplatePostProcessor.*;
import static com.foreach.across.modules.webcms.web.thymeleaf.WebCmsDialect.PREFIX;

/**
 * Enables generic {@link WebCmsComponentModel} rendering support from markup.
 * Uses processing instructions to signal how output handling should occur with regards to placeholders and auto-creation.
 * Not the easiest flow to understand as several combinations and levels of nesting are possible that have slightly different behaviour.
 * <p/>
 * The user documentation section on "building components from markup" is your best source of understanding the possible flows.
 *
 * @see PlaceholderAttributeProcessor
 * @see ComponentTemplatePostProcessor
 * @see PlaceholderTemplatePostProcessor
 * @see 0.0.2
 */
final class ComponentAttributeProcessor extends AbstractAttributeModelProcessor
{
	private static final AtomicInteger COUNTER = new AtomicInteger();

	private static final String ATTR_SEARCH_PARENTS = PREFIX + ":search-parent-scopes";
	private static final String ATTR_TYPE = PREFIX + ":type";
	private static final String ATTR_ALWAYS_REPLACE = PREFIX + ":always-replace";
	private static final String ATTR_PARSE_PLACEHOLDERS = PREFIX + ":parse-placeholders";

	static final String ATTR_COMPONENT = PREFIX + ":component";
	static final String ATTR_AUTO_CREATE = PREFIX + ":auto-create";
	static final String ATTR_SCOPE = PREFIX + ":scope";
	static final String ATTR_PARENT_CREATE_INCLUDE = PREFIX + ":parent-create-include";
	static final String ATTR_META_PREFIX = PREFIX + ":meta";
	static final String ATTR_PROP_PREFIX = PREFIX + ":prop";
	static final String ATTR_ATTR_PREFIX = PREFIX + ":attr";

	private static final Collection<String> FIXED_ATTRIBUTES_TO_REMOVE = Arrays.asList(
			ATTR_COMPONENT, ATTR_SCOPE, ATTR_SEARCH_PARENTS, ATTR_AUTO_CREATE, ATTR_TYPE, ATTR_ALWAYS_REPLACE, ATTR_PARSE_PLACEHOLDERS,
			ATTR_PARENT_CREATE_INCLUDE
	);

	ComponentAttributeProcessor() {
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
			ModelProcessingState modelProcessingState = ModelProcessingState.retrieve( templateContext );
			ExpressionParser expressionParser = ExpressionParser.create( templateContext );

			IProcessableElementTag elementTag = (IProcessableElementTag) model.get( 0 );
			boolean isStandaloneTag = elementTag instanceof IStandaloneElementTag;
			IModelFactory modelFactory = templateContext.getModelFactory();

			// during a parse placeholders phase, component attributes should be ignored and the template parsed for placeholders instead
			// all components inside (or on) the placeholder should be rendered

			boolean ignoreComponent = modelProcessingState.isParsePlaceholdersOnly();

			if ( !ignoreComponent ) {
				WebCmsComponentModelHierarchy components = modelProcessingState.getComponentModelHierarchy();

				String lookupScope = resolveLookupScope( elementTag, modelProcessingState );
				ComponentModelLookup lookupResult = lookupWebComponentModel( attributeValue, elementTag, components, lookupScope );

				WebCmsComponentAutoCreateQueue creationQueue = modelProcessingState.getAutoCreateQueue();
				WebCmsComponentAutoCreateTask currentCreationTask = creationQueue.getCurrentTask();

				boolean parsePlaceholders = elementTag.getAttribute( ATTR_PARSE_PLACEHOLDERS ) != null;
				boolean shouldRenderComponentOutput = currentCreationTask == null
						|| elementTag.hasAttribute( ATTR_PARENT_CREATE_INCLUDE )
						|| modelProcessingState.isInsidePlaceholder();

				String creationScope = resolveCreationScope( elementTag, modelProcessingState, lookupScope );

				boolean createProxy = !shouldRenderComponentOutput && WebCmsComponentUtils.isContainerType( currentCreationTask.getComponentType() );

				if ( lookupResult != null && lookupResult.hasComponentModel() ) {
					elementTag = renderComponentModel(
							(IEngineContext) templateContext,
							model,
							elementTag,
							modelFactory,
							lookupResult.getComponentModel(),
							parsePlaceholders,
							shouldRenderComponentOutput,
							createProxy
					);
				}
				else {
					boolean shouldRenderComponentDuringCreation = currentCreationTask == null || elementTag.hasAttribute( ATTR_PARENT_CREATE_INCLUDE );

					if ( creationScope != null ) {
						createProxy &= !CONTAINER_MEMBER_SCOPE.equals( creationScope );
						autoCreateComponent(
								model, attributeValue, elementTag, isStandaloneTag, modelFactory, parsePlaceholders, creationQueue,
								creationScope, shouldRenderComponentDuringCreation, createProxy, expressionParser
						);
					}
					else if ( elementTag.hasAttribute( ATTR_ALWAYS_REPLACE ) || !shouldRenderComponentDuringCreation ) {
						renderEmptyBody( model, elementTag );
					}
				}

				if ( !shouldRenderComponentOutput ) {
					if ( createProxy ) {
						model.insert( 1, modelFactory.createProcessingInstruction(
								COMPONENT_RENDER,
								new WebCmsContentMarker( "wcm:component", attributeValue + ",container,false" ).toString()
						) );
					}
					else {
						writeComponentContentMarker( model, modelFactory, lookupResult );
					}
				}
			}

			removeProcessedAttributesFromTag( model, elementTag, modelFactory );
		}
	}

	private String resolveLookupScope( IProcessableElementTag elementTag, ModelProcessingState modelProcessingState ) {
		IAttribute attribute = elementTag.getAttribute( ATTR_SCOPE );
		String scope = null;

		if ( attribute == null ) {
			if ( !modelProcessingState.isInsidePlaceholder() ) {
				WebCmsComponentAutoCreateTask currentTask = modelProcessingState.getAutoCreateQueue().getCurrentTask();

				if ( currentTask != null && WebCmsComponentUtils.isContainerType( currentTask.getComponentType() ) ) {
					scope = CONTAINER_MEMBER_SCOPE;
				}
			}
		}
		else {
			scope = attribute.getValue();
		}

		return scope;
	}

	private String resolveCreationScope( IProcessableElementTag elementTag, ModelProcessingState modelProcessingState, String lookupScope ) {
		String creationScope = null;
		IAttribute attribute = elementTag.getAttribute( ATTR_AUTO_CREATE );

		if ( attribute != null ) {
			creationScope = attribute.getValue();
			if ( creationScope == null ) {
				creationScope = lookupScope;
			}
			if ( creationScope == null ) {
				creationScope = modelProcessingState.getComponentModelHierarchy().getDefaultScope();
			}
		}
		else if ( CONTAINER_MEMBER_SCOPE.equals( lookupScope ) ) {
			creationScope = CONTAINER_MEMBER_SCOPE;
		}

		// don't trigger container auto-creation when inside placeholder
		if ( modelProcessingState.isInsidePlaceholder() && CONTAINER_MEMBER_SCOPE.equals( creationScope ) ) {
			return null;
		}

		return creationScope;
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
	                                  String creationScope,
	                                  boolean shouldRenderComponent,
	                                  boolean createProxy,
	                                  ExpressionParser expressionParser ) {
		String componentType = elementTag.getAttributeValue( ATTR_TYPE );
		WebCmsComponentAutoCreateTask task = queue.schedule( attributeValue, creationScope, componentType );

		// should also auto-create container members?
		if ( isStandaloneTag ) {
			renderEmptyBody( model, elementTag );
			model.add( modelFactory.createProcessingInstruction( START_INSTRUCTION, task.getTaskId() ) );
			model.add( modelFactory.createProcessingInstruction( STOP_INSTRUCTION, task.getTaskId() ) );

			if ( CONTAINER_MEMBER_SCOPE.equals( creationScope ) ) {
				model.add( createContainerComponentRenderInstruction( modelFactory, task ) );
			}
		}
		else {
			if ( parsePlaceholders ) {
				model.insert( 1, modelFactory.createProcessingInstruction( PlaceholderTemplatePostProcessor.START_PARSE_PLACEHOLDERS, "" ) );
			}

			model.insert( 1, modelFactory.createProcessingInstruction( START_INSTRUCTION, task.getTaskId() ) );
			model.insert( model.size() - 1, modelFactory.createProcessingInstruction( STOP_INSTRUCTION, task.getTaskId() ) );

			if ( createProxy ) {
				model.add( modelFactory.createProcessingInstruction( ComponentTemplatePostProcessor.CREATED_COMPONENT_PROXY, task.getTaskId() ) );
			}

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

			if ( CONTAINER_MEMBER_SCOPE.equals( creationScope ) ) {
				model.insert( model.size() - 1, createContainerComponentRenderInstruction( modelFactory, task ) );
			}

			if ( parsePlaceholders ) {
				model.insert( model.size() - 1,
				              modelFactory.createProcessingInstruction( PlaceholderTemplatePostProcessor.STOP_PARSE_PLACEHOLDERS, "" ) );
			}
		}

		attachAttributeValues( task, elementTag, expressionParser );
	}

	private void attachAttributeValues( WebCmsComponentAutoCreateTask task,
	                                    IProcessableElementTag elementTag,
	                                    ExpressionParser expressionParser ) {
		for ( IAttribute attr : elementTag.getAllAttributes() ) {
			String prefix = determineAttributePrefix( attr.getAttributeCompleteName() );

			if ( prefix != null ) {
				String propertyName = StringUtils.removeStart( attr.getAttributeCompleteName(), prefix );
				int colon = propertyName.indexOf( ':' );
				if ( colon >= 0 ) {
					propertyName = StringUtils.substring( propertyName, colon + 1 );
					Object attributeValue = expressionParser.parse( attr.getValue() );
					if ( StringUtils.isNotEmpty( propertyName ) ) {
						switch ( prefix ) {
							case ATTR_META_PREFIX:
								task.addAttributeValue( Attribute.METADATA, propertyName, attributeValue );
								break;
							case ATTR_PROP_PREFIX:
								task.addAttributeValue( Attribute.PROPERTY, propertyName, attributeValue );
								break;
							default:
								task.addAttributeValue( Attribute.ANY, propertyName, attributeValue );
						}
					}
				}
			}
		}
	}

	private String determineAttributePrefix( String attributeName ) {
		if ( attributeName.startsWith( ATTR_META_PREFIX ) ) {
			return ATTR_META_PREFIX;
		}
		else if ( attributeName.startsWith( ATTR_PROP_PREFIX ) ) {
			return ATTR_PROP_PREFIX;
		}
		else if ( attributeName.startsWith( ATTR_ATTR_PREFIX ) ) {
			return ATTR_ATTR_PREFIX;
		}

		return null;
	}

	private ITemplateEvent createContainerComponentRenderInstruction( IModelFactory modelFactory, WebCmsComponentAutoCreateTask task ) {
		return modelFactory.createProcessingInstruction(
				COMPONENT_RENDER,
				new WebCmsContentMarker( "wcm:component", task.getComponentName() + "," + task.getScopeName() + ",false" ).toString()
		);
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

	private IProcessableElementTag renderComponentModel( IEngineContext context,
	                                                     IModel model,
	                                                     IProcessableElementTag elementTag,
	                                                     IModelFactory modelFactory,
	                                                     WebCmsComponentModel component,
	                                                     boolean parsePlaceholders,
	                                                     boolean renderComponentOutput,
	                                                     boolean signalProxyCreation ) {
		if ( parsePlaceholders ) {
			model.insert( 1, modelFactory.createProcessingInstruction( PlaceholderTemplatePostProcessor.START_IGNORE_NON_PLACEHOLDERS, "" ) );
			model.insert( 1, modelFactory.createProcessingInstruction( PlaceholderTemplatePostProcessor.START_PARSE_PLACEHOLDERS, "" ) );

			if ( model.get( model.size() - 1 ) instanceof ICloseElementTag ) {
				model.remove( model.size() - 1 );
			}

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

		if ( signalProxyCreation ) {
			model.add( modelFactory.createProcessingInstruction( ComponentTemplatePostProcessor.EXISTING_COMPONENT_PROXY, component.getObjectId() ) );
		}

		if ( parsePlaceholders ) {
			// only decrease placeholder level after the actual component has been rendered
			model.add( modelFactory.createProcessingInstruction( PlaceholderTemplatePostProcessor.STOP_PARSE_PLACEHOLDERS, "" ) );
		}

		model.add( modelFactory.createCloseElementTag( elementTag.getElementCompleteName() ) );

		return elementTag;
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

		for ( String attributeToRemove : FIXED_ATTRIBUTES_TO_REMOVE ) {
			newFirstEvent = modelFactory.removeAttribute( newFirstEvent, attributeToRemove );
		}

		for ( IAttribute attr : elementTag.getAllAttributes() ) {
			if ( StringUtils.startsWithAny( attr.getAttributeCompleteName(), ATTR_META_PREFIX, ATTR_PROP_PREFIX, ATTR_ATTR_PREFIX ) ) {
				newFirstEvent = modelFactory.removeAttribute( newFirstEvent, attr.getAttributeCompleteName() );
			}
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
