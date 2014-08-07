package com.foreach.imageserver.core.rest.services;

import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.rest.request.ListResolutionsRequest;
import com.foreach.imageserver.core.rest.request.ViewImageRequest;
import com.foreach.imageserver.core.rest.response.ListResolutionsResponse;
import com.foreach.imageserver.core.rest.response.ViewImageResponse;
import com.foreach.imageserver.core.services.DtoUtil;
import com.foreach.imageserver.core.services.ImageContextService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import com.foreach.imageserver.dto.ImageResolutionDto;
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
public class ImageRestServiceImpl implements ImageRestService
{
	@Autowired
	private ImageContextService contextService;

	@Autowired
	private ImageService imageService;

	private String fallbackImageKey;

	public void setFallbackImageKey( String fallbackImageKey ) {
		this.fallbackImageKey = fallbackImageKey;
	}

	@Override
	public ViewImageResponse renderImage( ViewImageRequest request ) {
		ViewImageResponse response = new ViewImageResponse( request );

		Image image = imageService.getByExternalId( request.getExternalId() );

		if ( image == null ) {
			response.setImageDoesNotExist( true );
		}
		else {
			StreamImageSource imageSource = imageService.generateModification(
					image,
					request.getImageModificationDto(),
					DtoUtil.toBusiness( request.getImageVariantDto() )
			);

			if ( imageSource == null ) {
				response.setFailed( true );
			}
			else {
				response.setImageSource( imageSource );
			}
		}

		return response;
	}

	@Override
	public ViewImageResponse viewImage( ViewImageRequest request ) {
		ViewImageResponse response = new ViewImageResponse( request );

		Image image = imageService.getByExternalId( request.getExternalId() );

		if ( image == null
				&& StringUtils.isNotBlank( fallbackImageKey )
				&& !StringUtils.equals( request.getExternalId(), fallbackImageKey ) ) {
			image = imageService.getByExternalId( fallbackImageKey );
		}

		if ( image == null ) {
			response.setImageDoesNotExist( true );
		}
		else {
			ImageContext context = contextService.getByCode( request.getContext() );

			if ( context == null ) {
				response.setContextDoesNotExist( true );
			}
			else {
				ImageResolutionDto imageResolutionDto = request.getImageResolutionDto();
				ImageResolution imageResolution = contextService.getImageResolution(
						context.getId(), imageResolutionDto.getWidth(), imageResolutionDto.getHeight()
				);

				if ( imageResolution == null ) {
					response.setResolutionDoesNotExist( true );
				}
				else {
					StreamImageSource imageSource = imageService.getVariantImage(
							image, context, imageResolution, DtoUtil.toBusiness( request.getImageVariantDto() )
					);

					if ( imageSource == null ) {
						response.setFailed( true );
					}
					else {
						response.setImageSource( imageSource );
					}
				}
			}
		}

		return response;
	}

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
