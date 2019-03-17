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

import org.apache.commons.lang3.ArrayUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.*;
import org.thymeleaf.processor.element.AbstractElementModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.HashMap;
import java.util.Map;

import static com.foreach.across.modules.webcms.web.thymeleaf.WebCmsDialect.PREFIX;

/**
 * Takes care of translating a {@code <wcm:component name="cmp" />} tag to the equivalent {@code <th:block wcm:component="cmp" />}.
 * The complex logic of auto-creating components is mostly coordinated by the {@link ComponentAttributeProcessor}.
 *
 * @author Arne Vandamme
 * @see ComponentAttributeProcessor
 * @since 0.0.7
 */
final class ComponentElementProcessor extends AbstractElementModelProcessor
{
	private static String[] ATTRIBUTES_TO_PREFIX = new String[] {
			"search-parent-scopes", "type", "always-replace", "parse-placeholders", "auto-create", "scope", "parent-create-include",
			"auto-create-placeholder-parent", "never-replace"
	};

	ComponentElementProcessor() {
		super(
				TemplateMode.HTML,  // This processor will apply only to HTML mode
				PREFIX,             // Prefix to be applied to name for matching
				"component",        // Tag name: match specifically this tag
				true,               // Apply dialect prefix to tag name
				null,               // attribute name
				false,              // Apply prefix to attribute name
				0                   // Precedence (lower = higher)
		);
	}

	@Override
	protected void doProcess( ITemplateContext context, IModel model, IElementModelStructureHandler structureHandler ) {
		IModelFactory modelFactory = context.getModelFactory();

		IProcessableElementTag openTag = (IProcessableElementTag) model.get( 0 );
		boolean selfClosing = openTag instanceof IStandaloneElementTag;

		Map<String, String> convertedAttributes = convertAttributes( openTag.getAttributeMap() );

		IProcessableElementTag replacementTag = selfClosing
				? modelFactory.createStandaloneElementTag( "th:block", convertedAttributes, AttributeValueQuotes.DOUBLE, false, false )
				: modelFactory.createOpenElementTag( "th:block", convertedAttributes, AttributeValueQuotes.DOUBLE, false );

		model.replace( 0, replacementTag );

		if ( !selfClosing ) {
			model.replace( model.size() - 1, modelFactory.createCloseElementTag( "th:block" ) );
		}
	}

	private Map<String, String> convertAttributes( Map<String, String> attributeMap ) {
		Map<String, String> converted = new HashMap<>( attributeMap.size() );
		attributeMap.forEach( ( name, value ) -> {
			if ( name.contains( ":" ) ) {
				converted.put( name, value );
			}
			else if ( "name".equals( name ) ) {
				converted.put( ComponentAttributeProcessor.ATTR_COMPONENT, value );
			}
			else if ( ArrayUtils.contains( ATTRIBUTES_TO_PREFIX, name ) ) {
				converted.put( PREFIX + ":" + name, value );
			}
			else {
				converted.put( ComponentAttributeProcessor.ATTR_ATTR_PREFIX + ":" + name, value );
			}
		} );
		return converted;
	}
}
