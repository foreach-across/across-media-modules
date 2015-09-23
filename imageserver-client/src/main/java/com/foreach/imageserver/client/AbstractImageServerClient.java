package com.foreach.imageserver.client;

import com.foreach.imageserver.dto.*;
import com.foreach.imageserver.logging.LogHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

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
	                        int height ) {
		return imageUrl( imageId, context, new ImageResolutionDto( width, height ), new ImageVariantDto() );
	}

	public String imageUrl( String imageId,
	                        String context,
	                        String... size ) {
		return imageUrl( imageId, context, null, new ImageVariantDto(), size );
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
	                        int width,
	                        int height,
	                        ImageVariantDto imageVariant ) {
		return imageUrl( imageId, context, new ImageResolutionDto( width, height ), imageVariant );
	}

	@Override
	public String imageUrl( String imageId,
	                        String context,
	                        ImageTypeDto imageType,
	                        String... size
	) {
		return imageUrl( imageId, context, null, new ImageVariantDto( imageType ), size );
	}

	@Override
	public String imageUrl( String imageId,
	                        String context,
	                        ImageResolutionDto imageResolution,
	                        ImageVariantDto imageVariant,
	                        String... size ) {
		if ( StringUtils.isBlank( imageId ) || context == null || imageVariant == null ) {
			LOG.warn(
					"Null parameters not allowed - imageId={}, context={}, imageResolution={}, imageVariant={}, size={}",
					LogHelper.flatten( imageId, context, imageResolution, imageVariant, size ) );
		}
		if ( imageResolution == null && ( size == null || size.length == 0 ) ) {
			LOG.error(
					"Request does not contain an imageResolution or size - imageId={}, context={}, imageResolution={}, imageVariant={}, size={}",
					LogHelper.flatten( imageId, context, null, imageVariant, size ) );
			throw new IllegalArgumentException( "Request does not contain an imageResolution or size" );
		}

		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.set( "iid", imageId );
		queryParams.set( "context", StringUtils.defaultString( context ) );

		if ( imageResolution != null ) {
			addQueryParams( queryParams, imageResolution );
		}
		if ( size != null && size.length > 0 ) {
			queryParams.set( "size", StringUtils.join( size, "," ) );
		}

		addQueryParams( queryParams, imageVariant );

		return buildUri( ENDPOINT_IMAGE_VIEW, queryParams ).toString();
	}

	@Override
	public String imageUrl( String imageId, String context, String ratio, int screenWidth, ImageTypeDto imageType, int width, int height ) {
		return imageUrl( imageId, context, ratio, screenWidth, new ImageVariantDto( imageType, new DimensionsDto( width, height )) );
	}
	@Override
	public String imageUrl( String imageId, String context, String ratio, int screenWidth, ImageVariantDto imageVariant ) {
		if ( StringUtils.isBlank( imageId ) || context == null || ratio == null || imageVariant == null ) {
			LOG.warn( "Null parameters not allowed -- imageId={}, context={}, ratio={}, imageVariant={}", imageId,
			          context, ratio, imageVariant );
		}
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.set( "iid", imageId );
		queryParams.set( "context", StringUtils.defaultString( context ) );
		queryParams.set( "ratio", ratio );
		queryParams.set( "width", Integer.toString( screenWidth ) );
		addQueryParams( queryParams, imageVariant );
		return buildUri( ENDPOINT_IMAGE_VIEW, queryParams ).toString();
	}

	protected URI buildUri( String path, MultiValueMap<String, String> queryParams ) {
		return buildUri( path, queryParams, imageServerUrl );
	}

	protected URI buildUri( String path, MultiValueMap<String, String> queryParams, String host ) {
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl( host ).path( "/" ).path( path );
		for ( String key : queryParams.keySet() ) {
			for ( int n = 0; n < queryParams.get( key ).size(); ++n ) {
				uri.queryParam( key, queryParams.get( key ).get( n ) );
			}
		}
		return uri.build().toUri();
	}

	protected void addQueryParams( MultiValueMap<String, String> queryParams, ImageResolutionDto imageResolution ) {
		if ( imageResolution.getWidth() != 0 ) {
			queryParams.set( "width", Integer.toString( imageResolution.getWidth() ) );
		}
		if ( imageResolution.getHeight() != 0 ) {
			queryParams.set( "height", Integer.toString( imageResolution.getHeight() ) );
		}
	}

	protected void addQueryParams( MultiValueMap<String, String> queryParams, ImageVariantDto imageVariant ) {
		ImageTypeDto imageTypeDto = imageVariant.getImageType();
		if ( imageTypeDto != null ) {
			queryParams.set( "imageType", imageVariant.getImageType().toString() );
		}
		DimensionsDto boundaries = imageVariant.getBoundaries();
		if ( boundaries != null ) {
			queryParams.set( "boundaries.width", String.valueOf( boundaries.getWidth() ) );
			queryParams.set( "boundaries.height", String.valueOf( boundaries.getHeight() ) );
		}
	}

	protected void addQueryParams( MultiValueMap<String, String> queryParams,
	                               ImageModificationDto imageModification ) {
		ImageResolutionDto resolution = imageModification.getResolution();
		DimensionsDto boundaries = imageModification.getBoundaries();
		CropDto crop = imageModification.getCrop();
		DimensionsDto density = imageModification.getDensity();

		queryParams.add( "resolution.width", Integer.toString( resolution.getWidth() ) );
		queryParams.add( "resolution.height", Integer.toString( resolution.getHeight() ) );

		queryParams.add( "crop.x", Integer.toString( crop.getX() ) );
		queryParams.add( "crop.y", Integer.toString( crop.getY() ) );
		queryParams.add( "crop.width", Integer.toString( crop.getWidth() ) );
		queryParams.add( "crop.height", Integer.toString( crop.getHeight() ) );
		queryParams.add( "crop.source.width", Integer.toString( crop.getSource().getWidth() ) );
		queryParams.add( "crop.source.height", Integer.toString( crop.getSource().getHeight() ) );
		queryParams.add( "crop.box.width", Integer.toString( crop.getBox().getWidth() ) );
		queryParams.add( "crop.box.height", Integer.toString( crop.getBox().getHeight() ) );
		queryParams.add( "density.width", Integer.toString( density.getWidth() ) );
		queryParams.add( "density.height", Integer.toString( density.getHeight() ) );

		queryParams.add( "boundaries.width", Integer.toString( boundaries.getWidth() ) );
		queryParams.add( "boundaries.height", Integer.toString( boundaries.getHeight() ) );
	}

	protected void addQueryParams( MultiValueMap<String, String> queryParams,
	                               List<ImageModificationDto> imageModifications ) {
		for ( ImageModificationDto imageModificationDto : imageModifications ) {
			addQueryParams( queryParams, imageModificationDto );
		}
	}
}
