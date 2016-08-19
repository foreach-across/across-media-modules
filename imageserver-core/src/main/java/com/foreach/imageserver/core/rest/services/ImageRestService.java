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
	/**
	 * Will always attempt to render the image based on the request parameters.
	 * Does not perform the security checks, unlike {@link #viewImage(ViewImageRequest)}.
	 *
	 * @param request containing the parameters of the image
	 * @return response containing failure feedback or the resulting image
	 */
	ViewImageResponse renderImage( ViewImageRequest request );

	/**
	 * Returns the image matching the request parameters, only if the image passes the
	 * security checks.  Either the resolution requested must match one of the registered
	 * resolutions for the context, or the security check callback returns {@code true}.
	 * The latter will only be used if the server is not operating in strict mode.
	 *
	 * @param request containing the parameters of the image
	 * @return response containing failure feedback or the resulting image
	 */
	ViewImageResponse viewImage( ViewImageRequest request );

	PregenerateResolutionsResponse pregenerateResolutions( String imageId );

	ListModificationsResponse listModifications( ListModificationsRequest request );

	ListResolutionsResponse listResolutions( ListResolutionsRequest request );

	RegisterModificationResponse registerModifications( RegisterModificationRequest request );
}
