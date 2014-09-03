package com.foreach.imageserver.core.rest.services;

import com.foreach.imageserver.core.rest.request.ListModificationsRequest;
import com.foreach.imageserver.core.rest.request.ListResolutionsRequest;
import com.foreach.imageserver.core.rest.request.RegisterModificationRequest;
import com.foreach.imageserver.core.rest.request.ViewImageRequest;
import com.foreach.imageserver.core.rest.response.*;

/**
 * @author Arne Vandamme
 */
public interface ImageRestService
{
	ViewImageResponse renderImage( ViewImageRequest request );

	ViewImageResponse viewImage( ViewImageRequest request );

	PregenerateResolutionsResponse pregenerateResolutions( String imageId );

	ListModificationsResponse listModifications( ListModificationsRequest request );

	ListResolutionsResponse listResolutions( ListResolutionsRequest request );

	RegisterModificationResponse registerModification( RegisterModificationRequest request );
}
