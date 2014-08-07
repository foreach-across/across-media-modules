package com.foreach.imageserver.core.rest.services;

import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.rest.request.ListResolutionsRequest;
import com.foreach.imageserver.core.rest.response.ListResolutionsResponse;
import com.foreach.imageserver.core.services.DtoUtil;
import com.foreach.imageserver.core.services.ImageContextService;
import com.foreach.imageserver.core.services.ImageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Arne Vandamme
 */
@Service
public class ResolutionRestServiceImpl implements ResolutionRestService
{
	@Autowired
	private ImageContextService contextService;

	@Autowired
	private ImageService imageService;

	@Override
	public ListResolutionsResponse listResolutions( ListResolutionsRequest request ) {
		ListResolutionsResponse response = new ListResolutionsResponse( request );

		Collection<ImageResolution> imageResolutions = null;

		if ( StringUtils.isNotBlank( request.getContext() ) ) {
			ImageContext context = contextService.getByCode( request.getContext() );

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
				imageResolutions = filterNonConfigurableResolutions( imageResolutions );
			}

			response.setImageResolutions( DtoUtil.toDto( imageResolutions ) );
		}

		return response;
	}

	private Collection<ImageResolution> filterNonConfigurableResolutions( Collection<ImageResolution> imageResolutions ) {
		List<ImageResolution> configurableResolutions = new ArrayList<>( imageResolutions.size() );

		for ( ImageResolution resolution : imageResolutions ) {
			if ( resolution.isConfigurable() ) {
				configurableResolutions.add( resolution );
			}
		}

		return configurableResolutions;
	}
}
