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
import com.foreach.across.modules.webcms.domain.component.model.WebComponentModelWriter;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
public class TextWebComponentModelWriter implements WebComponentModelWriter<TextWebComponentModel>
{
	@Override
	public boolean supports( WebComponentModel componentModel ) {
		return TextWebComponentModel.class.isInstance( componentModel );
	}

	@Override
	public void writeToComponent( TextWebComponentModel componentModel, WebCmsComponent component ) {
		component.setBody( componentModel.getContent() );
	}
}
