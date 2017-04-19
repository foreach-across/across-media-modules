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

package com.foreach.across.modules.webcms.domain.component.text;

import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.model.WebComponentModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Getter
@Setter
@NoArgsConstructor
public class TextWebComponentModel extends WebComponentModel
{
	/**
	 * Attributes supported on a {@link com.foreach.across.modules.webcms.domain.component.WebCmsComponentType}.
	 */
	public interface Attributes
	{
		/**
		 * Type of component: plain-text, rich-text or html are supported
		 */
		String TYPE = "type";

		/**
		 * If set to false, the component will behave as a single line of text.
		 * In all other cases it should behave as a multi-line text component.
		 */
		String MULTI_LINE = "multiLine";

		/**
		 * Optionally specify an additional text profile this component should use.
		 */
		String PROFILE = "profile";

		/**
		 * Size indicator: number of rows the component should have by default.
		 * Only applicable in case of a multiline component.
		 */
		String ROWS = "rows";
	}

	/**
	 * Basic supported text component types.
	 */
	public enum MarkupType
	{
		PLAIN_TEXT( "plain-text" ),
		RICH_TEXT( "rich-text" ),
		MARKUP( "markup" );

		private final String attributeValue;

		MarkupType( String attributeValue ) {
			this.attributeValue = attributeValue;
		}

		public String asAttributeValue() {
			return attributeValue;
		}

		public static MarkupType forComponent( WebCmsComponent component ) {
			return fromAttributeValue( component.getComponentType().getAttribute( Attributes.TYPE ) );
		}

		public static MarkupType fromAttributeValue( String attributeValue ) {
			return Stream.of( values() )
			             .filter( v -> StringUtils.equals( v.attributeValue, attributeValue ) )
			             .findFirst()
			             .orElse( null );
		}
	}

	private String content;

	private String profile;

	private boolean multiLine;

	private MarkupType markupType;

	public TextWebComponentModel( WebCmsComponent component ) {
		super( component );
	}
}
