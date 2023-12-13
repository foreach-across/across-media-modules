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

import com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateQueue;
import com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateTask;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.WebEngineContext;
import org.thymeleaf.model.*;
import org.thymeleaf.processor.AbstractProcessor;
import org.thymeleaf.processor.element.IElementModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.processor.element.MatchingAttributeName;
import org.thymeleaf.processor.element.MatchingElementName;
import org.thymeleaf.templatemode.TemplateMode;

import static com.foreach.across.modules.webcms.web.thymeleaf.WebCmsDialect.PREFIX;

/**
 * Adds support for elements that render component attributes: {@code <wcm:component-attribute>},
 * {@code <wcm:component-metadata>} and {@code wcm:component-property}.
 *
 * @author Arne Vandamme
 * @see ComponentAttributeTemplatePostProcessor
 * @since 0.0.7
 */
final class ComponentAttributeElementProcessor extends AbstractProcessor implements IElementModelProcessor
{
	private final MatchingElementName matchingElementName = MatchingElementName.forAllElementsWithPrefix( TemplateMode.HTML, PREFIX );

	@RequiredArgsConstructor
	private enum Type
	{
		ATTRIBUTE( "wcm:component-attribute", WebCmsComponentAutoCreateTask.Attribute.ANY ),
		PROPERTY( "wcm:component-property", WebCmsComponentAutoCreateTask.Attribute.PROPERTY ),
		METADATA( "wcm:component-metadata", WebCmsComponentAutoCreateTask.Attribute.METADATA );

		private final String tag;
		private final WebCmsComponentAutoCreateTask.Attribute attributeType;
	}

	ComponentAttributeElementProcessor() {
		super( TemplateMode.HTML, 1000 );
	}

	@Override
	public void process( ITemplateContext context, IModel model, IElementModelStructureHandler structureHandler ) {
		ITemplateEvent templateEvent = model.get( 0 );

		if ( templateEvent instanceof IProcessableElementTag ) {
			IProcessableElementTag tag = (IProcessableElementTag) templateEvent;
			Type attributeType = resolveAttributeType( tag );

			if ( attributeType != null ) {
				ApplicationContext appCtx = RequestContextUtils.findWebApplicationContext( ( (WebEngineContext) context ).getRequest() );
				WebCmsComponentAutoCreateQueue queue = appCtx.getBean( WebCmsComponentAutoCreateQueue.class );

				WebCmsComponentAutoCreateTask currentTask = queue.getCurrentTask();

				if ( currentTask != null ) {
					ExpressionParser expressionParser = ExpressionParser.create( context );

					IAttribute nameAttribute = tag.getAttribute( "name" );

					if ( nameAttribute == null ) {
						throw new IllegalStateException( attributeType.tag + " element requires a 'name' attribute" );
					}

					String name = (String) expressionParser.parse( nameAttribute.getValue() );

					IAttribute valueAttribute = tag.getAttribute( "value" );

					if ( valueAttribute != null ) {
						Object value = expressionParser.parse( valueAttribute.getValue() );
						currentTask.addAttributeValue( attributeType.attributeType, name, value );
						model.reset();
					}
					else {
						IModelFactory modelFactory = context.getModelFactory();
						model.remove( 0 );
						if ( tag instanceof IOpenElementTag ) {
							model.remove( model.size() - 1 );
						}
						model.insert( 0, modelFactory.createOpenElementTag( "th:block", tag.getAttributeMap(), AttributeValueQuotes.DOUBLE, false ) );
						model.insert( 0,
						              modelFactory.createProcessingInstruction(
								              ComponentAttributeTemplatePostProcessor.START_ATTRIBUTE, attributeType.attributeType.name() + ":" + name
						              )
						);
						model.add( modelFactory.createCloseElementTag( "th:block" ) );
						model.add( modelFactory.createProcessingInstruction( ComponentAttributeTemplatePostProcessor.STOP_ATTRIBUTE, "" ) );
					}
				}
				else {
					// no component being created, simply remove the output
					model.reset();
				}
			}
		}
	}

	private Type resolveAttributeType( IProcessableElementTag tag ) {
		if ( Type.METADATA.tag.equals( tag.getElementCompleteName() ) ) {
			return Type.METADATA;
		}
		if ( Type.PROPERTY.tag.equals( tag.getElementCompleteName() ) ) {
			return Type.PROPERTY;
		}
		if ( Type.ATTRIBUTE.tag.equals( tag.getElementCompleteName() ) ) {
			return Type.ATTRIBUTE;
		}
		return null;
	}

	@Override
	public MatchingElementName getMatchingElementName() {
		return matchingElementName;
	}

	@Override
	public MatchingAttributeName getMatchingAttributeName() {
		return null;
	}
}
