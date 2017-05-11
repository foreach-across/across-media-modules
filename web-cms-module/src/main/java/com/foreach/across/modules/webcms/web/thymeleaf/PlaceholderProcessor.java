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

import com.foreach.across.modules.webcms.domain.component.placeholder.WebCmsPlaceholderContentModel;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.WebEngineContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.*;
import org.thymeleaf.processor.element.AbstractAttributeModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import static com.foreach.across.modules.webcms.web.thymeleaf.WebCmsDialect.PREFIX;

/**
 * Renders the markup represented by the element to a placeholder variable instead of the direct output.
 */
class PlaceholderProcessor extends AbstractAttributeModelProcessor
{
	PlaceholderProcessor() {
		super(
				TemplateMode.HTML,              // This processor will apply only to HTML mode
				PREFIX,        // Prefix to be applied to name for matching
				null,               // Tag name: match specifically this tag
				false,          // Apply dialect prefix to tag name
				"placeholder",              // No attribute name: will match by tag name
				true,          // No prefix to be applied to attribute name
				1000,
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
			String placeholderName = attributeValue;

			IProcessableElementTag elementTag = (IProcessableElementTag) model.get( 0 );
			boolean isStandaloneTag = elementTag instanceof IStandaloneElementTag;
			IModelFactory modelFactory = context.getModelFactory();

			IProcessableElementTag newFirstEvent = modelFactory.removeAttribute( elementTag, attributeName );
			if ( newFirstEvent != elementTag ) {
				model.replace( 0, newFirstEvent );
			}

			ApplicationContext applicationContext = RequestContextUtils.findWebApplicationContext( ( (WebEngineContext) context ).getRequest() );
			WebCmsPlaceholderContentModel placeholders = applicationContext.getBean( WebCmsPlaceholderContentModel.class );

			placeholders.setPlaceholderContent( placeholderName, model.cloneModel() );

			model.reset();
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
}
