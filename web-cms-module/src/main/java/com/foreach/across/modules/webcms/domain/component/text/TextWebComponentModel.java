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
import com.foreach.across.modules.webcms.domain.component.WebComponentModel;
import lombok.Data;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Data
public class TextWebComponentModel implements WebComponentModel
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

	public enum TextType
	{
		PLAIN_TEXT,
		RICH_TEXT,
		HTML
	}

	private String content;

	private String profile;

	private boolean multiLine;

	private TextType textType;

	public void writeToComponent( WebCmsComponent component ) {
		component.setBody( content );
	}
}
