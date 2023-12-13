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

package test.component.container;

import com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test.AbstractWebCmsComponentModelRenderingTest;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
public class TestContainerWebCmsComponentModelRendering extends AbstractWebCmsComponentModelRenderingTest
{
	@Autowired
	private WebCmsComponentModelService componentModelService;

	@Test
	public void membersAreRenderedInOrder() {
		ContainerWebCmsComponentModel container = componentModelService.createComponentModel( "container", ContainerWebCmsComponentModel.class );
		container.setMarkup( "custom markup" );

		TextWebCmsComponentModel text = componentModelService.createComponentModel( "html", TextWebCmsComponentModel.class );
		text.setContent( "<strong>some html text</strong>" );

		TextWebCmsComponentModel textTwo = componentModelService.createComponentModel( "html", TextWebCmsComponentModel.class );
		textTwo.setContent( "other" );

		container.addMember( text );
		container.addMember( textTwo );

		renderAndExpect( container, "<strong>some html text</strong>other" );
	}

	@Test
	public void markupIsUsedIfContainerSupportsIt() {
		ContainerWebCmsComponentModel container = componentModelService.createComponentModel( "container", ContainerWebCmsComponentModel.class );
		container.getComponent()
		         .setComponentType(
				         container.getComponentType()
				                  .toBuilder()
				                  .attribute( ContainerWebCmsComponentModel.SUPPORTS_MARKUP_ATTRIBUTE, "true" )
				                  .build()
		         );

		container.setMarkup( "1: @@wcm:component(two,container,false)@@, 2: @@wcm:component(one,container,false)@@" );

		TextWebCmsComponentModel text = componentModelService.createComponentModel( "html", TextWebCmsComponentModel.class );
		text.setName( "one" );
		text.setContent( "<strong>some html text</strong>" );

		TextWebCmsComponentModel textTwo = componentModelService.createComponentModel( "html", TextWebCmsComponentModel.class );
		textTwo.setName( "two" );
		textTwo.setContent( "other" );

		container.addMember( text );
		container.addMember( textTwo );

		renderAndExpect( container, "1: other, 2: <strong>some html text</strong>" );
	}

	@Test
	public void customTemplate() {
		ContainerWebCmsComponentModel container = componentModelService.createComponentModel( "container", ContainerWebCmsComponentModel.class );
		container.setRenderTemplate( "th/test/fragments :: snippet(container=${component})" );

		TextWebCmsComponentModel body = componentModelService.createComponentModel( "html", TextWebCmsComponentModel.class );
		body.setName( "body" );
		body.setContent( "<strong>some html text</strong>" );

		TextWebCmsComponentModel title = componentModelService.createComponentModel( "html", TextWebCmsComponentModel.class );
		title.setName( "title" );
		title.setRenderTemplate( "th/test/fragments :: renderText" );
		title.setContent( "title text" );

		container.addMember( body );
		container.addMember( title );

		renderAndExpect( container, "<div>" +
				"<h1 class='some-header'>title text</h1>" +
				"<p class='body'><strong>some html text</strong></p>" +
				"</div>" );
	}

	@Test
	public void templateFromComponentType() {
		ContainerWebCmsComponentModel container = componentModelService.createComponentModel( "container", ContainerWebCmsComponentModel.class );
		container.getComponent()
		         .setComponentType(
				         container.getComponentType()
				                  .toBuilder()
				                  .attribute( WebCmsComponentModel.TEMPLATE_ATTRIBUTE, "th/test/fragments :: snippet(container=${component})" )
				                  .build()
		         );

		TextWebCmsComponentModel body = componentModelService.createComponentModel( "html", TextWebCmsComponentModel.class );
		body.setName( "body" );
		body.setContent( "<strong>other html text</strong>" );

		TextWebCmsComponentModel title = componentModelService.createComponentModel( "html", TextWebCmsComponentModel.class );
		title.setName( "title" );
		title.setRenderTemplate( "th/test/fragments :: renderText" );
		title.setContent( "mytitle" );

		container.addMember( body );
		container.addMember( title );

		renderAndExpect( container, "<div>" +
				"<h1 class='some-header'>mytitle</h1>" +
				"<p class='body'><strong>other html text</strong></p>" +
				"</div>" );
	}
}
