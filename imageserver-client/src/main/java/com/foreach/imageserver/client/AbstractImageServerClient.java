package com.foreach.imageserver.client;

import com.foreach.imageserver.dto.*;
import com.foreach.imageserver.logging.LogHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides the common ImageServerClient methods that are endpoint independent.
 *
 * @author Arne Vandamme
 */
public abstract class AbstractImageServerClient implements ImageServerClient
{
	private String imageServerUrl;

	protected AbstractImageServerClient( String imageServerUrl ) {
		this.imageServerUrl = imageServerUrl;
	}

	public String getImageServerUrl() {
		return imageServerUrl;
	}

	public void setImageServerUrl( String imageServerUrl ) {
		this.imageServerUrl = imageServerUrl;
	}

	@Override
	public String imageUrl( String imageId,
	                        String context,
	                        int width,
	                        int height
	                        ) {
		return imageUrl( imageId, context, new ImageResolutionDto( width, height ), new ImageVariantDto( null ) );
	}

	@Override
	public String imageUrl( String imageId,
	                        String context,
	                        int width,
	                        int height,
	                        ImageTypeDto imageType ) {
		return imageUrl( imageId, context, new ImageResolutionDto( width, height ), new ImageVariantDto( imageType ) );
	}

	@Override
	public String imageUrl( String imageId,
	                        String context,
	                        ImageResolutionDto imageResolution,
	                        ImageVariantDto imageVariant ) {
		if ( StringUtils.isBlank( imageId ) || context == null || imageResolution == null || imageVariant == null ) {
			LOG.warn(
					"Null parameters not allowed - ImageServerClientImpl#imageUrl: imageId={}, context={}, imageResolution={}, imageVariant={}",
					LogHelper.flatten( imageId, context, imageResolution, imageVariant ) );
		}

		Map<String, String> queryParams = new LinkedHashMap<>();
		queryParams.put( "iid", imageId );
		queryParams.put( "context", StringUtils.defaultString( context ) );
		addQueryParams( queryParams, imageResolution );
		addQueryParams( queryParams, imageVariant );

		return buildUri( ENDPOINT_IMAGE_VIEW, queryParams ).toString();
	}

	protected URI buildUri( String path, Map<String, String> queryParams ) {
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl( imageServerUrl ).path( "/" ).path( path );
		for ( Map.Entry<String, String> param : queryParams.entrySet() ) {
			uri.queryParam( param.getKey(), param.getValue() );
		}
		return uri.build().toUri();
	}

	protected void addQueryParams( Map<String, String> queryParams, ImageResolutionDto imageResolution ) {
		if ( imageResolution.getWidth() != 0 ) {
			queryParams.put( "width", Integer.toString( imageResolution.getWidth() ) );
		}
		if ( imageResolution.getHeight() != 0 ) {
			queryParams.put( "height", Integer.toString( imageResolution.getHeight() ) );
		}
	}

	protected void addQueryParams( Map<String, String> queryParams, ImageVariantDto imageVariant ) {
	ImageTypeDto imageTypeDto = imageVariant.getImageType();
		if( imageTypeDto != null ) {
			queryParams.put( "imageType", imageVariant.getImageType().toString() );
		}
	}

	protected void addQueryParams( Map<String, String> queryParams,
	                               ImageModificationDto imageModification ) {
		ImageResolutionDto resolution = imageModification.getResolution();
		DimensionsDto boundaries = imageModification.getBoundaries();
		CropDto crop = imageModification.getCrop();
		DimensionsDto density = imageModification.getDensity();

		queryParams.put( "resolution.width", Integer.toString( resolution.getWidth() ) );
		queryParams.put( "resolution.height", Integer.toString( resolution.getHeight() ) );

		queryParams.put( "crop.x", Integer.toString( crop.getX() ) );
		queryParams.put( "crop.y", Integer.toString( crop.getY() ) );
		queryParams.put( "crop.width", Integer.toString( crop.getWidth() ) );
		queryParams.put( "crop.height", Integer.toString( crop.getHeight() ) );
		queryParams.put( "crop.source.width", Integer.toString( crop.getSource().getWidth() ) );
		queryParams.put( "crop.source.height", Integer.toString( crop.getSource().getHeight() ) );
		queryParams.put( "crop.box.width", Integer.toString( crop.getBox().getWidth() ) );
		queryParams.put( "crop.box.height", Integer.toString( crop.getBox().getHeight() ) );
		queryParams.put( "density.width", Integer.toString( density.getWidth() ) );
		queryParams.put( "density.height", Integer.toString( density.getHeight() ) );

		queryParams.put( "boundaries.width", Integer.toString( boundaries.getWidth() ) );
		queryParams.put( "boundaries.height", Integer.toString( boundaries.getHeight() ) );
	}
}
