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

package com.foreach.across.modules.webcms.web.thymeleaf;

import com.foreach.across.modules.web.thymeleaf.ThymeleafModelBuilder;
import com.foreach.across.modules.webcms.domain.component.WebCmsContentMarker;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;

/**
 * API for rendering a content marker to the output.
 *
 * The {@link WebCmsComponentModelViewElementModelWriter} will retrieve all {@link WebCmsComponentContentMarkerRenderer}
 * beans and find the appropriate one when rendering a particular component model.
 * <p/>
 * Implementations can be ordered as the first renderer that supports a particular marker will be used to render it.
 *
 * @author Arne Vandamme
 * @see ThymeleafModelBuilder
 * @since 0.0.2
 */
public interface WebCmsComponentContentMarkerRenderer<T extends WebCmsComponentModel>
{
	/**
	 * Can this renderer write this component model?
	 *
	 * @param componentModel to render
	 * @return true if renderer can write it
	 */
	boolean supports( WebCmsComponentModel componentModel, WebCmsContentMarker marker );

	/**
	 * Build the Thymeleaf model for the {@link WebCmsComponentModel}.
	 *
	 * @param component to render
	 * @param model     to add the output instructions to
	 */
	void writeMarkerOutput( T component, WebCmsContentMarker marker, ThymeleafModelBuilder model );
}
