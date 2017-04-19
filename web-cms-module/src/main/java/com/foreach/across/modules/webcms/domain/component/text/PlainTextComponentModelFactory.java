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
import com.foreach.across.modules.webcms.domain.component.WebComponentModelFactory;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
public class PlainTextComponentModelFactory implements WebComponentModelFactory<TextWebComponentModel>
{
	@Override
	public boolean supports( WebCmsComponent component ) {
		return "text-field".equals( component.getComponentType().getTypeKey() );
	}

	@Override
	public TextWebComponentModel createWebComponentModel( WebCmsComponent component ) {
		TextWebComponentModel model = new TextWebComponentModel();
		model.setContent( component.getBody() );
		model.setTextType( TextWebComponentModel.TextType.PLAIN_TEXT );
		model.setMultiLine( true );
		model.setProfile( component.getComponentType().getTypeKey() );

		return model;
	}
}
