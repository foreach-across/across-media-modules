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

package test.component.text;

import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import test.AbstractWebCmsComponentModelRenderingTest;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
public class TestTextWebCmsComponentModelRendering extends AbstractWebCmsComponentModelRenderingTest
{
	@Autowired
	private WebCmsComponentModelService componentModelService;

	@Test
	public void simpleTextComponent() {
		TextWebCmsComponentModel text = componentModelService.createComponentModel( "html", TextWebCmsComponentModel.class );
		text.setContent( "<strong>some html text</strong>" );

		renderAndExpect( text, "<strong>some html text</strong>" );
	}

	@Test
	public void templateFromComponentType() {
		TextWebCmsComponentModel text = componentModelService.createComponentModel( "html", TextWebCmsComponentModel.class );

		text.getComponent()
		    .setComponentType(
				    text.getComponentType()
				        .toBuilder()
				        .attribute( WebCmsComponentModel.TEMPLATE_ATTRIBUTE, "th/test/fragments :: renderText" )
				        .build()
		    );

		text.setContent( "header text" );
		renderAndExpect( text, "<h1 class='some-header'>header text</h1>" );
	}

	@Test
	public void customTemplate() {
		TextWebCmsComponentModel text = componentModelService.createComponentModel( "html", TextWebCmsComponentModel.class );
		text.setContent( "header text" );
		text.setRenderTemplate( "th/test/fragments :: renderText" );

		renderAndExpect( text, "<h1 class='some-header'>header text</h1>" );
	}
}
