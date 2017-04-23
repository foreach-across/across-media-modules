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

import com.foreach.across.modules.web.ui.elements.TextViewElement;
import com.foreach.across.modules.webcms.domain.component.model.WebComponentModel;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.thymeleaf.context.IEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.WebEngineContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Enables generic {@link com.foreach.across.modules.web.ui.ViewElement} rendering support.
 */
class WebComponentModelProcessor extends AbstractElementTagProcessor
{
	private static final String ATTRIBUTE_COMPONENT = "component";

	WebComponentModelProcessor() {
		super(
				TemplateMode.HTML,              // This processor will apply only to HTML mode
				WebCmsDialect.PREFIX,        // Prefix to be applied to name for matching
				null,               // Tag name: match specifically this tag
				false,          // Apply dialect prefix to tag name
				ATTRIBUTE_COMPONENT,              // No attribute name: will match by tag name
				true,          // No prefix to be applied to attribute name
				10000
		);
	}

	@Override
	protected final void doProcess( final ITemplateContext context, final IProcessableElementTag tag, final IElementTagStructureHandler structureHandler ) {
		WebComponentModel componentModel = retrieveComponentFromAttribute( context, tag );
		ApplicationContext appCtx = RequestContextUtils.findWebApplicationContext( ( (WebEngineContext) context ).getRequest() );

		String atrId = "_generatedComponentName" + System.currentTimeMillis();
		( (IEngineContext) context ).setVariable( atrId, TextViewElement.text( componentModel.toString() ) );

		final IModelFactory modelFactory = context.getModelFactory();

		final IModel model = modelFactory.createModel();
		model.add( modelFactory.createOpenElementTag( "across:view", "element", "${" + atrId + "}", false ) );
		model.add( modelFactory.createCloseElementTag( "across:view" ) );

		structureHandler.replaceWith( model, true );
	}

	private WebComponentModel retrieveComponentFromAttribute( ITemplateContext context, IProcessableElementTag element ) {
		String expr = element.getAttributeValue( WebCmsDialect.PREFIX, ATTRIBUTE_COMPONENT );
		IStandardExpressionParser parser = StandardExpressions.getExpressionParser( context.getConfiguration() );
		IStandardExpression expression = parser.parseExpression( context, expr );

		Object component = expression.execute( context );

		if ( component instanceof WebComponentModel ) {
			return (WebComponentModel) component;
		}

		throw new IllegalArgumentException(
				WebCmsDialect.PREFIX + ":" + ATTRIBUTE_COMPONENT + " requires a value of type WebComponentModel"
		);
	}
}
