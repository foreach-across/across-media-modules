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
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

/**
 * Represents a text component that can be either plain text, rich text or markup (defined by {@link MarkupType}.
 * In all cases an optional profile is supported that can indicate the sub-types of content that are supported.
 * <p/>
 * The actual {@link #getComponentType()} can contain attributes that will determine the additional property values of this component.
 * See the {@link Attributes} interface for attribute names that are supported by default.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Getter
@Setter
@NoArgsConstructor
public class TextWebCmsComponentModel extends WebCmsComponentModel
{
	/**
	 * Attributes supported on a {@link com.foreach.across.modules.webcms.domain.component.WebCmsComponentType}.
	 */
	public interface Attributes
	{
		/**
		 * Type of component: plain-text, rich-text or html are supported
		 */
		String TYPE = TYPE_ATTRIBUTE;

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
		 * Only applicable in case of a multi-line component.
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

	/**
	 * The actual text content of this component.
	 */
	private String content;

	/**
	 * Optional profile hint for management or parsing UIs.
	 * Determined by the {@link #getComponentType()}.
	 */
	private String profile;

	/**
	 * Hint if this text component allows multiple lines or not.
	 * Usually determined by the {@link #getComponentType}.
	 */
	private boolean multiLine = true;

	/**
	 * Specific type of markup this component contains.
	 * Usually determined by the {@link #getComponentType}.
	 */
	private MarkupType markupType = MarkupType.MARKUP;

	public TextWebCmsComponentModel( WebCmsComponent component ) {
		super( component );
	}

	/**
	 * @return true if there is no actual content set
	 */
	public boolean isEmpty() {
		return StringUtils.isEmpty( content );
	}

	@Override
	public TextWebCmsComponentModel asComponentTemplate() {
		TextWebCmsComponentModel template = new TextWebCmsComponentModel( getComponent().asTemplate() );
		template.content = content;
		template.markupType = markupType;
		template.multiLine = multiLine;
		template.profile = profile;

		return template;
	}
}
