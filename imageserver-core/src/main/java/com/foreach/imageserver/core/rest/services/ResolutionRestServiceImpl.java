package com.foreach.imageserver.core.rest.services;

import com.foreach.imageserver.core.business.Context;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.rest.request.ListResolutionsRequest;
import com.foreach.imageserver.core.rest.response.ListResolutionsResponse;
import com.foreach.imageserver.core.services.ContextService;
import com.foreach.imageserver.core.services.DtoUtil;
import com.foreach.imageserver.core.services.ImageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Arne Vandamme
 */
@Service
public class ResolutionRestServiceImpl implements ResolutionRestService
{
	@Autowired
	private ContextService contextService;

	@Autowired
	private ImageService imageService;

	@Override
	public ListResolutionsResponse listResolutions( ListResolutionsRequest request ) {
		ListResolutionsResponse response = new ListResolutionsResponse( request );

		Collection<ImageResolution> imageResolutions = null;

		if ( StringUtils.isNotBlank( request.getContextCode() ) ) {
			Context context = contextService.getByCode( request.getContextCode() );

			if ( context == null ) {
				response.setContextDoesNotExist( true );
			}
			else {
				imageResolutions = contextService.getImageResolutions( context.getId() );
			}
		}
		else {
			imageResolutions = imageService.getAllResolutions();
		}

		if ( imageResolutions != null && !imageResolutions.isEmpty() ) {
			if ( request.isConfigurableOnly() ) {
				removeNonConfigurableResolutions( imageResolutions );
			}

			response.setImageResolutions( DtoUtil.toDto( imageResolutions ) );
		}

		return response;
	}

	private void removeNonConfigurableResolutions( Collection<ImageResolution> imageResolutions ) {
		Iterator<ImageResolution> iterator = imageResolutions.iterator();

		while ( iterator.hasNext() ) {
			ImageResolution resolution = iterator.next();

			if ( !resolution.isConfigurable() ) {
				iterator.remove();
			}
		}
	}
}
