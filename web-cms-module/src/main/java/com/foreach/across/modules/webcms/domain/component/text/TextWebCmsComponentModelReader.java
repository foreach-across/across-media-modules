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
import com.foreach.across.modules.webcms.domain.component.model.AbstractWebCmsComponentModelReader;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel.Attributes;
import lombok.val;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
public class TextWebCmsComponentModelReader extends AbstractWebCmsComponentModelReader<TextWebCmsComponentModel>
{
	@Override
	public boolean supports( WebCmsComponent component ) {
		return TextWebCmsComponentModel.MarkupType.forComponent( component ) != null;
	}

	@Override
	public TextWebCmsComponentModel buildComponentModel( WebCmsComponent component ) {
		val attributes = component.getComponentType().getAttributes();

		TextWebCmsComponentModel model = new TextWebCmsComponentModel( component );
		model.setContent( component.getBody() );
		model.setMarkupType( TextWebCmsComponentModel.MarkupType.forComponent( component ) );
		model.setMultiLine( !"false".equalsIgnoreCase( attributes.get( Attributes.MULTI_LINE ) ) );
		model.setProfile( attributes.get( Attributes.PROFILE ) );

		return model;
	}
}
