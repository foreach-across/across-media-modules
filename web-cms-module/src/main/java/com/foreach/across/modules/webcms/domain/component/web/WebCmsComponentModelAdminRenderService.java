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

package com.foreach.across.modules.webcms.domain.component.web;

import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilder;
import com.foreach.across.modules.webcms.domain.component.UnknownWebCmsComponentModelException;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * Central API for building the administration UI for editing components.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Service
public final class WebCmsComponentModelAdminRenderService
{
	private Collection<WebCmsComponentModelContentAdminRenderer> contentRenderers = Collections.emptyList();
	private Collection<WebCmsComponentModelMetadataAdminRenderer> metadataRenderers = Collections.emptyList();

	@SuppressWarnings("unchecked")
	public ViewElementBuilder createContentViewElementBuilder( WebCmsComponentModel componentModel, String controlNamePrefix ) {
		return contentRenderers.stream()
		                       .filter( r -> r.supports( componentModel ) )
		                       .findFirst()
		                       .orElseThrow( () -> new UnknownWebCmsComponentModelException( componentModel ) )
		                       .createContentViewElementBuilder( componentModel, controlNamePrefix );
	}

	@SuppressWarnings("unchecked")
	public ViewElementBuilder createMetadataViewElementBuilder( WebCmsComponentModel componentModel, String controlNamePrefix ) {
		if ( componentModel.hasMetadata() ) {
			return metadataRenderers.stream()
			                        .filter( r -> r.supports( componentModel, componentModel.getMetadata() ) )
			                        .findFirst()
			                        .map( r -> r.createMetadataViewElementBuilder( componentModel, componentModel.getMetadata(), controlNamePrefix ) )
			                        .orElseGet( ContainerViewElementBuilder::new );
		}

		return new ContainerViewElementBuilder();
	}

	@Autowired
	void setContentRenderers( @RefreshableCollection(includeModuleInternals = true) Collection<WebCmsComponentModelContentAdminRenderer> contentRenderers ) {
		this.contentRenderers = contentRenderers;
	}

	@Autowired
	void setMetadataRenderers( @RefreshableCollection(includeModuleInternals = true) Collection<WebCmsComponentModelMetadataAdminRenderer> metadataRenderers ) {
		this.metadataRenderers = metadataRenderers;
	}
}
