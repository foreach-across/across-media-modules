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

package test.component.placeholder;

import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.placeholder.PlaceholderWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.placeholder.WebCmsPlaceholderContentModel;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test.AbstractWebCmsComponentModelRenderingTest;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
public class TestPlaceholderWebCmsComponentModelRendering extends AbstractWebCmsComponentModelRenderingTest
{
	@Autowired
	private WebCmsComponentModelService componentModelService;

	@Autowired
	private WebCmsPlaceholderContentModel placeholderContentModel;

	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Test
	public void emptyPlaceholder() {
		renderAndExpect( new PlaceholderWebCmsComponentModel(), "" );
	}

	@Test
	public void noPlaceholderValue() {
		PlaceholderWebCmsComponentModel component = new PlaceholderWebCmsComponentModel();
		component.setPlaceholderName( "my-placeholder" );
		renderAndExpect( component, "" );
	}

	@Test
	public void simpleStringPlaceholder() {
		PlaceholderWebCmsComponentModel component = new PlaceholderWebCmsComponentModel();
		component.setPlaceholderName( "my-placeholder" );

		renderAndExpect(
				component,
				model -> placeholderContentModel.setPlaceholderContent( "my-placeholder", "some placeholder value" ),
				"some placeholder value"
		);
	}

	@Test
	public void shadowedPlaceholderValue() {
		PlaceholderWebCmsComponentModel component = new PlaceholderWebCmsComponentModel();
		component.setPlaceholderName( "my-placeholder" );

		renderAndExpect(
				component,
				model -> {
					placeholderContentModel.setPlaceholderContent( "my-placeholder", "some placeholder value" );
					placeholderContentModel.increaseLevel();
					placeholderContentModel.setPlaceholderContent( "my-placeholder", 123L );
				},
				"123"
		);
	}

	@Test
	public void otherComponentModelAsValue() {
		TextWebCmsComponentModel text = componentModelService.createComponentModel( "html", TextWebCmsComponentModel.class );
		text.setContent( "<strong>some html text</strong>" );

		PlaceholderWebCmsComponentModel component = new PlaceholderWebCmsComponentModel();
		component.setPlaceholderName( "my-placeholder" );

		renderAndExpect(
				component,
				model -> placeholderContentModel.setPlaceholderContent( "my-placeholder", text ),
				"<strong>some html text</strong>"
		);
	}

	@Test
	public void templateFromComponentType() {
		PlaceholderWebCmsComponentModel placeholder = componentModelService.createComponentModel( "placeholder", PlaceholderWebCmsComponentModel.class );

		placeholder.getComponent()
		           .setComponentType(
				           placeholder.getComponentType()
				                      .toBuilder()
				                      .attribute( WebCmsComponentModel.TEMPLATE_ATTRIBUTE, "th/test/fragments :: renderPlaceholder" )
				                      .build()
		           );

		placeholder.setPlaceholderName( "my-placeholder" );
		renderAndExpect(
				placeholder,
				m -> placeholderContentModel.setPlaceholderContent( "my-placeholder", "placeholder content" ),
				"<h1 class='some-header'>placeholder content</h1>"
		);
	}
}
