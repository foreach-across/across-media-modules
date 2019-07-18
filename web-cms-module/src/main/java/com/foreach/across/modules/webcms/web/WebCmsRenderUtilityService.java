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

package com.foreach.across.modules.webcms.web;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelHierarchy;
import com.foreach.across.modules.webcms.domain.component.proxy.ProxyWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpointService;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsUriComponentsService;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.across.modules.webcms.domain.image.component.ImageWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.image.connector.WebCmsImageConnector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

/**
 * Contains utility methods most often used for rendering.
 * This object is also exposed as expression object <strong>#wcm</strong> on thymeleaf.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Service("wcmRenderUtils")
@RequiredArgsConstructor
public class WebCmsRenderUtilityService
{
	private final WebCmsImageConnector imageConnector;
	private final WebCmsComponentModelHierarchy componentModelHierarchy;
	private final WebCmsEndpointService endpointService;
	private final WebCmsUriComponentsService uriComponentsService;

	/**
	 * Build a url to the original image file of the image configured on a {@link WebCmsImage}.
	 *
	 * @param imageComponent that contains the image
	 * @return image url or {@code null} if no image was present
	 */
	public String imageUrl( ImageWebCmsComponentModel imageComponent ) {
		return imageUrl( imageComponent, WebCmsImageConnector.ORIGINAL_WIDTH, WebCmsImageConnector.ORIGINAL_HEIGHT );
	}

	/**
	 * Build a url to the image file of the image configured on a {@link WebCmsImage}.
	 * If possible the resulting image should be scaled down to fit in the box dimensions specified.
	 * Note that it depends on the actual {@link WebCmsImageConnector} used if this will be the case.
	 *
	 * @param imageComponent that contains the image
	 * @param boxWidth       image should be scaled down to this maximum width
	 * @param boxHeight      image should be scaled down to this maximum height
	 * @return image url or {@code null} if no image was present
	 */
	public String imageUrl( ImageWebCmsComponentModel imageComponent, int boxWidth, int boxHeight ) {
		return imageComponent.isEmpty() ? null : imageUrl( imageComponent.getImage(), boxWidth, boxHeight );
	}

	/**
	 * Build a url to the original image file of a {@link WebCmsImage}.
	 *
	 * @param image to generate the url for
	 * @return image url
	 */
	public String imageUrl( WebCmsImage image ) {
		return imageUrl( image, WebCmsImageConnector.ORIGINAL_WIDTH, WebCmsImageConnector.ORIGINAL_HEIGHT );
	}

	/**
	 * Build a url to the image file of the {@link WebCmsImage}.
	 * If possible the resulting image should be scaled down to fit in the box dimensions specified.
	 * Note that it depends on the actual {@link WebCmsImageConnector} used if this will be the case.
	 *
	 * @param image     to generate the url for
	 * @param boxWidth  image should be scaled down to this maximum width
	 * @param boxHeight image should be scaled down to this maximum height
	 * @return image url
	 */
	public String imageUrl( WebCmsImage image, int boxWidth, int boxHeight ) {
		return imageConnector.buildImageUrl( image, boxWidth, boxHeight );
	}

	/**
	 * Retrieve a component by name from the default scope.
	 * Will search parent scopes.
	 *
	 * @param componentName name of the component
	 * @return model or {@code null} if not found
	 */
	public WebCmsComponentModel component( String componentName ) {
		return componentModelHierarchy.get( componentName );
	}

	/**
	 * Retrieve a component by name from the specified scope.
	 * Will not search parent scopes.
	 *
	 * @param componentName name of the component
	 * @param scopeName     scope in which to search for the component
	 * @return model or {@code null} if not found
	 */
	public WebCmsComponentModel componentFromScope( String componentName, String scopeName ) {
		return componentModelHierarchy.getFromScope( componentName, scopeName );
	}

	/**
	 * Retrieve a component by name from the specified scope.
	 * Depending on the value of the searchParentScopes parameter, parent scopes will be searched for the component.
	 *
	 * @param componentName      name of the component
	 * @param scopeName          scope in which to search for the component
	 * @param searchParentScopes should parent scopes be searched
	 * @return model or {@code null} if not found
	 */
	public WebCmsComponentModel componentFromScope( String componentName, String scopeName, boolean searchParentScopes ) {
		return componentModelHierarchy.getFromScope( componentName, scopeName, searchParentScopes );
	}

	/**
	 * Retrieve a container member by name.
	 * Allows the container itself to be null or not a container model, in both cases the member returned will be {@code null}.
	 * If the container is a proxy component, the target component will be investigated.
	 *
	 * @param componentName name of the member component
	 * @param container     in which to search
	 * @return model of {@code null} if not found or container was {@code null}
	 */
	public WebCmsComponentModel member( String componentName, WebCmsComponentModel container ) {
		WebCmsComponentModel target = container instanceof ProxyWebCmsComponentModel ? ( (ProxyWebCmsComponentModel) container ).getTarget() : container;
		if ( target instanceof ContainerWebCmsComponentModel ) {
			return ( (ContainerWebCmsComponentModel) target ).getMember( componentName );
		}
		return null;
	}

	/**
	 * Retrieve the member components of a container.
	 * If the container is null or not a container model, the returned list will always be empty.
	 * If the container is a proxy component, the target component will be used instead.
	 *
	 * @param container for which to fetch the members
	 * @return list of member components
	 */
	public List<WebCmsComponentModel> members( WebCmsComponentModel container ) {
		WebCmsComponentModel target = container instanceof ProxyWebCmsComponentModel ? ( (ProxyWebCmsComponentModel) container ).getTarget() : container;
		if ( target instanceof ContainerWebCmsComponentModel ) {
			return ( (ContainerWebCmsComponentModel) target ).getMembers();
		}
		return Collections.emptyList();
	}

	/**
	 * Retrieve the primary URL for a specific asset.
	 *
	 * @param asset to get the primary URL for
	 * @return url or {@code null}
	 */
	public String url( WebCmsAsset asset ) {
		return uriComponentsService.buildUriComponents( asset )
		                           .map( UriComponentsBuilder::toUriString )
		                           .orElse( null );
	}
}
