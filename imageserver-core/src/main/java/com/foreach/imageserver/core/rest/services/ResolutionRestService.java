package com.foreach.imageserver.core.rest.services;

import com.foreach.imageserver.core.rest.request.ListResolutionsRequest;
import com.foreach.imageserver.core.rest.response.ListResolutionsResponse;

/**
 * @author Arne Vandamme
 */
public interface ResolutionRestService
{
	ListResolutionsResponse listResolutions( ListResolutionsRequest request );
}
