package com.foreach.imageserver.core.rest.services;

import com.foreach.imageserver.core.rest.request.ListModificationsRequest;
import com.foreach.imageserver.core.rest.request.ListResolutionsRequest;
import com.foreach.imageserver.core.rest.request.RegisterModificationRequest;
import com.foreach.imageserver.core.rest.request.ViewImageRequest;
import com.foreach.imageserver.core.rest.response.ListModificationsResponse;
import com.foreach.imageserver.core.rest.response.ListResolutionsResponse;
import com.foreach.imageserver.core.rest.response.RegisterModificationResponse;
import com.foreach.imageserver.core.rest.response.ViewImageResponse;

/**
 * @author Arne Vandamme
 */
public interface ImageRestService
{
	ViewImageResponse renderImage( ViewImageRequest request );

	ViewImageResponse viewImage( ViewImageRequest request );

	ListModificationsResponse listModifications( ListModificationsRequest request );

	ListResolutionsResponse listResolutions( ListResolutionsRequest request );

	RegisterModificationResponse registerModification( RegisterModificationRequest request );
}
