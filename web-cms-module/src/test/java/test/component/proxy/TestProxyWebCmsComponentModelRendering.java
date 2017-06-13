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

package test.component.proxy;

import com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.proxy.ProxyWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test.AbstractWebCmsComponentModelRenderingTest;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
public class TestProxyWebCmsComponentModelRendering extends AbstractWebCmsComponentModelRenderingTest
{
	@Autowired
	private WebCmsComponentModelService componentModelService;

	@Test
	public void emptyProxyRendersNothing() {
		ProxyWebCmsComponentModel proxy = componentModelService.createComponentModel( "proxy", ProxyWebCmsComponentModel.class );
		renderAndExpect( proxy, "" );
	}

	@Test
	public void proxyTextComponent() {
		TextWebCmsComponentModel text = componentModelService.createComponentModel( "plain-text", TextWebCmsComponentModel.class );
		text.setContent( "my text" );

		ProxyWebCmsComponentModel proxy = componentModelService.createComponentModel( "proxy", ProxyWebCmsComponentModel.class );
		proxy.setTarget( text );

		renderAndExpect( proxy, "my text" );
	}

	@Test
	public void proxyContainer() {
		ContainerWebCmsComponentModel container = componentModelService.createComponentModel( "container", ContainerWebCmsComponentModel.class );

		TextWebCmsComponentModel text = componentModelService.createComponentModel( "html", TextWebCmsComponentModel.class );
		text.setContent( "<strong>some html text</strong>" );

		TextWebCmsComponentModel textTwo = componentModelService.createComponentModel( "html", TextWebCmsComponentModel.class );
		textTwo.setContent( "other" );

		container.addMember( text );
		container.addMember( textTwo );

		ProxyWebCmsComponentModel proxy = componentModelService.createComponentModel( "proxy", ProxyWebCmsComponentModel.class );
		proxy.setTarget( container );

		renderAndExpect( proxy, "<strong>some html text</strong>other" );
	}

	@Test
	public void customTemplate() {
		TextWebCmsComponentModel text = componentModelService.createComponentModel( "plain-text", TextWebCmsComponentModel.class );
		text.setContent( "my text" );

		ProxyWebCmsComponentModel proxy = componentModelService.createComponentModel( "proxy", ProxyWebCmsComponentModel.class );
		proxy.setRenderTemplate( "th/test/fragments :: customProxy" );
		proxy.setTarget( text );

		renderAndExpect( proxy, "<h5>my text</h5>" );
	}

	@Test
	public void templateFromComponentType() {
		TextWebCmsComponentModel text = componentModelService.createComponentModel( "plain-text", TextWebCmsComponentModel.class );
		text.setContent( "my text" );

		ProxyWebCmsComponentModel proxy = componentModelService.createComponentModel( "proxy", ProxyWebCmsComponentModel.class );
		proxy.getComponent()
		     .setComponentType(
				     proxy.getComponentType()
				          .toBuilder()
				          .attribute( WebCmsComponentModel.TEMPLATE_ATTRIBUTE, "th/test/fragments :: customProxy" )
				          .build()
		     );
		proxy.setTarget( text );

		renderAndExpect( proxy, "<h5>my text</h5>" );
	}
}
