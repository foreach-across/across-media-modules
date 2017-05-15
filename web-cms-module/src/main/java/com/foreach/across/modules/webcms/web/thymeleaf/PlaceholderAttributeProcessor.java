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

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.*;
import org.thymeleaf.processor.element.AbstractAttributeModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import static com.foreach.across.modules.webcms.web.thymeleaf.ComponentAttributesProcessor.MARKER_NEVER_REPLACE;
import static com.foreach.across.modules.webcms.web.thymeleaf.WebCmsDialect.PREFIX;

/**
 * Renders the markup represented by the element to a placeholder variable instead of the direct output.
 */
final class PlaceholderAttributeProcessor extends AbstractAttributeModelProcessor
{
	PlaceholderAttributeProcessor() {
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
	                          String placeholderName,
	                          IElementModelStructureHandler structureHandler ) {
		if ( model.size() > 0 && model.get( 0 ) instanceof IProcessableElementTag ) {
			IProcessableElementTag elementTag = (IProcessableElementTag) model.get( 0 );
			IModelFactory modelFactory = context.getModelFactory();

			IProcessableElementTag newFirstEvent = modelFactory.removeAttribute( elementTag, attributeName );
			if ( newFirstEvent != elementTag ) {
				model.replace( 0, newFirstEvent );
			}

			if ( placeholderName != null ) {
				enableDisabledComponentBlocks( model, modelFactory );

				model.insert( 0, modelFactory.createProcessingInstruction( PlaceholderTemplatePostProcessor.START_PLACEHOLDER, placeholderName ) );
				model.add( modelFactory.createProcessingInstruction( PlaceholderTemplatePostProcessor.STOP_PLACEHOLDER, placeholderName ) );
			}
		}
	}

	private void enableDisabledComponentBlocks( IModel model, IModelFactory modelFactory ) {
		// all components inside (or on) the placeholder should be rendered - even if the ComponentAttributeProcessor had put them hidden
		for ( int i = 0; i < model.size(); i++ ) {
			ITemplateEvent event = model.get( i );
			if ( event instanceof IOpenElementTag || event instanceof IStandaloneElementTag ) {
				IProcessableElementTag openElementTag = (IProcessableElementTag) event;
				if ( openElementTag.hasAttribute( MARKER_NEVER_REPLACE ) ) {
					model.replace( i, modelFactory.removeAttribute( openElementTag, MARKER_NEVER_REPLACE ) );
				}
			}
		}
	}
}
