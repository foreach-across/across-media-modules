package be.mediafin.imageserver.client;

import be.mediafin.imageserver.logging.LogHelper;
import com.foreach.imageserver.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;

/**
 * Provides the common ImageServerClient methods that are endpoint independent.
 *
 * @author Arne Vandamme
 */
public abstract class AbstractImageServerClient implements ImageServerClient
{
	protected final Logger LOG = LoggerFactory.getLogger( getClass() );

	private String imageServerUrl;

	protected AbstractImageServerClient( String imageServerUrl ) {
		this.imageServerUrl = imageServerUrl;
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

		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.set( "iid", imageId );
		queryParams.set( "context", context );
		addQueryParams( queryParams, imageResolution );
		addQueryParams( queryParams, imageVariant );

		//WebResource resource = getResource( "view", queryParams );
		// resource.getURI().toString();

		return buildUri( ENDPOINT_VIEW_IMAGE, queryParams ).toString();
	}

	protected URI buildUri( String path, MultiValueMap<String, String> queryParams ) {
		return UriComponentsBuilder.fromHttpUrl( imageServerUrl )
		                           .path( path )
		                           .queryParams( queryParams )
		                           .build()
		                           .toUri();
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
		queryParams.set( "imageType", imageVariant.getImageType().toString() );
	}

	protected void addQueryParams( MultiValueMap<String, String> queryParams,
	                               ImageModificationDto imageModification ) {
		ImageResolutionDto resolution = imageModification.getResolution();
		DimensionsDto boundaries = imageModification.getBoundaries();
		CropDto crop = imageModification.getCrop();
		DimensionsDto density = imageModification.getDensity();

		queryParams.set( "resolution.width", Integer.toString( resolution.getWidth() ) );
		queryParams.set( "resolution.height", Integer.toString( resolution.getHeight() ) );

		queryParams.set( "crop.x", Integer.toString( crop.getX() ) );
		queryParams.set( "crop.y", Integer.toString( crop.getY() ) );
		queryParams.set( "crop.width", Integer.toString( crop.getWidth() ) );
		queryParams.set( "crop.height", Integer.toString( crop.getHeight() ) );
		queryParams.set( "crop.source.width", Integer.toString( crop.getSource().getWidth() ) );
		queryParams.set( "crop.source.height", Integer.toString( crop.getSource().getHeight() ) );
		queryParams.set( "crop.box.width", Integer.toString( crop.getBox().getWidth() ) );
		queryParams.set( "crop.box.height", Integer.toString( crop.getBox().getHeight() ) );
		queryParams.set( "density.width", Integer.toString( density.getWidth() ) );
		queryParams.set( "density.height", Integer.toString( density.getHeight() ) );

		queryParams.set( "boundaries.width", Integer.toString( boundaries.getWidth() ) );
		queryParams.set( "boundaries.height", Integer.toString( boundaries.getHeight() ) );
	}

	// TODO: remove
	protected void addQueryParams( MultivaluedMap<String, String> queryParams, ImageResolutionDto imageResolution ) {
		if ( imageResolution.getWidth() != 0 ) {
			queryParams.putSingle( "width", Integer.toString( imageResolution.getWidth() ) );
		}
		if ( imageResolution.getHeight() != 0 ) {
			queryParams.putSingle( "height", Integer.toString( imageResolution.getHeight() ) );
		}
	}

	protected void addQueryParams( MultivaluedMap<String, String> queryParams, ImageVariantDto imageVariant ) {
		queryParams.putSingle( "imageType", imageVariant.getImageType().toString() );
	}

	protected void addQueryParams( MultivaluedMap<String, String> queryParams,
	                               ImageModificationDto imageModification ) {
		ImageResolutionDto resolution = imageModification.getResolution();
		DimensionsDto boundaries = imageModification.getBoundaries();
		CropDto crop = imageModification.getCrop();
		DimensionsDto density = imageModification.getDensity();

		queryParams.putSingle( "resolution.width", Integer.toString( resolution.getWidth() ) );
		queryParams.putSingle( "resolution.height", Integer.toString( resolution.getHeight() ) );

		queryParams.putSingle( "crop.x", Integer.toString( crop.getX() ) );
		queryParams.putSingle( "crop.y", Integer.toString( crop.getY() ) );
		queryParams.putSingle( "crop.width", Integer.toString( crop.getWidth() ) );
		queryParams.putSingle( "crop.height", Integer.toString( crop.getHeight() ) );
		queryParams.putSingle( "crop.source.width", Integer.toString( crop.getSource().getWidth() ) );
		queryParams.putSingle( "crop.source.height", Integer.toString( crop.getSource().getHeight() ) );
		queryParams.putSingle( "crop.box.width", Integer.toString( crop.getBox().getWidth() ) );
		queryParams.putSingle( "crop.box.height", Integer.toString( crop.getBox().getHeight() ) );
		queryParams.putSingle( "density.width", Integer.toString( density.getWidth() ) );
		queryParams.putSingle( "density.height", Integer.toString( density.getHeight() ) );

		queryParams.putSingle( "boundaries.width", Integer.toString( boundaries.getWidth() ) );
		queryParams.putSingle( "boundaries.height", Integer.toString( boundaries.getHeight() ) );
	}

}
