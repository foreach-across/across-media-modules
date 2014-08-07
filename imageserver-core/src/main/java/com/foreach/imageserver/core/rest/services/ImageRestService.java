package com.foreach.imageserver.core.rest.services;

import com.foreach.imageserver.core.rest.request.ListResolutionsRequest;
import com.foreach.imageserver.core.rest.request.ViewImageRequest;
import com.foreach.imageserver.core.rest.response.ListResolutionsResponse;
import com.foreach.imageserver.core.rest.response.ViewImageResponse;

/**
 * @author Arne Vandamme
 */
public interface ImageRestService
{
	ViewImageResponse renderImage( ViewImageRequest request );

	ViewImageResponse viewImage( ViewImageRequest request );

	ListResolutionsResponse listResolutions( ListResolutionsRequest request );
}
