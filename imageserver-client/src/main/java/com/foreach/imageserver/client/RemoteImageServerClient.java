package com.foreach.imageserver.client;

import com.foreach.imageserver.dto.*;
import com.foreach.imageserver.logging.LogHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.List;

/**
 * Represents a client for a remote ImageServer endpoint.
 */
public class RemoteImageServerClient extends AbstractImageServerClient
{
	private static final Logger LOG = LoggerFactory.getLogger( RemoteImageServerClient.class );

	private static final class ResponseTypes
	{
		private ResponseTypes() {
		}

		private static final ParameterizedTypeReference<JsonResponse<List<ImageResolutionDto>>> RESOLUTIONS =
				new ParameterizedTypeReference<JsonResponse<List<ImageResolutionDto>>>()
				{
				};

		private static final ParameterizedTypeReference<JsonResponse<List<ImageModificationDto>>> MODIFICATIONS =
				new ParameterizedTypeReference<JsonResponse<List<ImageModificationDto>>>()
				{
				};

		private static final ParameterizedTypeReference<JsonResponse<Object>> OBJECT =
				new ParameterizedTypeReference<JsonResponse<Object>>()
				{
				};

		private static final ParameterizedTypeReference<JsonResponse<ImageInfoDto>> IMAGE_INFO =
				new ParameterizedTypeReference<JsonResponse<ImageInfoDto>>()
				{
				};
	}

	private final String imageServerAccessToken;

	private final RestTemplate restTemplate;

	public RemoteImageServerClient( String imageServerEndpoint, String imageServerAccessToken ) {
		super( imageServerEndpoint );

		this.imageServerAccessToken = imageServerAccessToken;

		this.restTemplate = new RestTemplate();
	}

	@Override
	public InputStream imageStream( String imageId,
	                                String context,
	                                Integer width,
	                                Integer height,
	                                ImageTypeDto imageType ) {
		return imageStream( imageId, context, new ImageResolutionDto( width, height ),
		                    new ImageVariantDto( imageType ) );
	}

	@Override
	public InputStream imageStream( String imageId,
	                                String context,
	                                ImageResolutionDto imageResolution,
	                                ImageVariantDto imageVariant ) {
		if ( StringUtils.isBlank( imageId ) || context == null || imageResolution == null || imageVariant == null ) {
			LOG.warn(
					"Null parameters not allowed - ImageServerClientImpl#imageStream: imageId={}, context={}, imageResolution={}, imageVariant={}",
					LogHelper.flatten( imageId, context, imageResolution, imageVariant ) );
		}

		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.set( "iid", imageId );
		queryParams.set( "context", context );
		addQueryParams( queryParams, imageResolution );
		addQueryParams( queryParams, imageVariant );

		return new ByteArrayInputStream( httpGet( ENDPOINT_IMAGE_VIEW, queryParams, byte[].class ) );
	}

	@Override
	public InputStream imageStream( String imageId,
	                                ImageModificationDto imageModificationDto,
	                                ImageVariantDto imageVariant ) {
		if ( StringUtils.isBlank( imageId ) || imageModificationDto == null || imageVariant == null ) {
			LOG.warn(
					"Null parameters not allowed - ImageServerClientImpl#imageStream: imageId={}, imageModificationDto={}, imageResolution={}, imageVariant={}",
					LogHelper.flatten( imageId, imageModificationDto, imageVariant ) );
		}

		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.set( "token", imageServerAccessToken );
		queryParams.set( "iid", imageId );
		addQueryParams( queryParams, imageModificationDto );
		addQueryParams( queryParams, imageVariant );

		return new ByteArrayInputStream( httpGet( ENDPOINT_IMAGE_RENDER, queryParams, byte[].class ) );
	}

	@Override
	public ImageInfoDto loadImage( String imageId, byte[] imageBytes ) {
		return loadImage( imageId, imageBytes, null );
	}

	@Override
	public ImageInfoDto loadImage( String imageId, byte[] imageBytes, Date imageDate ) {
		if ( StringUtils.isBlank( imageId ) ) {
			throw new ImageServerException( "You must specify an imageId when loading an image." );
		}
		if ( imageBytes == null || imageBytes.length == 0 ) {
			throw new ImageServerException( "Unable to load an image with empty byte data." );
		}

		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.set( "token", imageServerAccessToken );
		queryParams.set( "iid", imageId );
		if ( imageDate != null ) {
			queryParams.set( "imageTimestamp", Long.toString( imageDate.getTime() ) );
		}

		MultiValueMap<String, Object> bodyParts = new LinkedMultiValueMap<>();
		bodyParts.set( "imageData", new ByteArrayResource( imageBytes )
		{
			@Override
			public String getFilename() {
				return "imageData";
			}
		} );

		try {
			return httpPost( ENDPOINT_IMAGE_LOAD, queryParams, bodyParts, ResponseTypes.IMAGE_INFO );
		}
		catch ( RuntimeException e ) {
			LOG.error(
					"Loading image caused exception - ImageServerClientImpl#loadImage: imageId={}, imageBytes={}, imageDate={}",
					LogHelper.flatten( imageId, imageBytes.length, imageDate ) );
			throw e;
		}
	}

	@Override
	public boolean imageExists( String imageId ) {
		return imageInfo( imageId ).isExisting();
	}

	@Override
	public ImageInfoDto imageInfo( String imageId ) {
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.set( "token", imageServerAccessToken );
		queryParams.set( "iid", imageId );

		return httpGet( ENDPOINT_IMAGE_INFO, queryParams, ResponseTypes.IMAGE_INFO );
	}

	@Override
	public List<ImageResolutionDto> pregenerateResolutions( String imageId ) {
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.set( "token", imageServerAccessToken );
		queryParams.set( "iid", imageId );

		return httpGet( ENDPOINT_IMAGE_PREGENERATE, queryParams, ResponseTypes.RESOLUTIONS );
	}

	@Override
	public void registerImageModification( String imageId,
	                                       String context,
	                                       ImageModificationDto imageModification ) {
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.set( "token", imageServerAccessToken );
		queryParams.set( "iid", imageId );
		queryParams.set( "context", context );

		addQueryParams( queryParams, imageModification );

		httpGet( ENDPOINT_MODIFICATION_REGISTER, queryParams, ResponseTypes.OBJECT );
	}

	@Override
	public void registerImageModifications( String imageId,
	                                       String context,
	                                       List<ImageModificationDto> imageModifications ) {
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.set( "token", imageServerAccessToken );
		queryParams.set( "iid", imageId );
		queryParams.set( "context", context );

		addQueryParams( queryParams, imageModifications );

		httpGet( ENDPOINT_MODIFICATION_REGISTER_LIST, queryParams, ResponseTypes.OBJECT );
	}

	@Override
	public List<ImageResolutionDto> listAllowedResolutions( String context ) {
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.set( "token", imageServerAccessToken );
		queryParams.set( "context", context );

		return httpGet( ENDPOINT_RESOLUTION_LIST, queryParams, ResponseTypes.RESOLUTIONS );
	}

	@Override
	public List<ImageResolutionDto> listConfigurableResolutions( String context ) {
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.set( "token", imageServerAccessToken );
		queryParams.set( "context", context );
		queryParams.set( "configurableOnly", "true" );

		return httpGet( ENDPOINT_RESOLUTION_LIST, queryParams, ResponseTypes.RESOLUTIONS );
	}

	@Override
	public List<ImageModificationDto> listModifications( String imageId, String context ) {
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.set( "token", imageServerAccessToken );
		queryParams.set( "iid", imageId );
		queryParams.set( "context", context );

		return httpGet( ENDPOINT_MODIFICATION_LIST, queryParams, ResponseTypes.MODIFICATIONS );
	}

	protected <T> T httpGet( String path, MultiValueMap<String, String> queryParams, Class<T> responseType ) {
		URI url = buildUri( path, queryParams );
		HttpEntity<?> request = new HttpEntity<MultiValueMap<?, ?>>( new LinkedMultiValueMap<String, String>() );

		ResponseEntity<T> response = restTemplate.exchange( url, HttpMethod.GET, request, responseType );

		return response.getBody();
	}

	protected <T> T httpGet( String path,
	                         MultiValueMap<String, String> queryParams,
	                         ParameterizedTypeReference<JsonResponse<T>> responseType ) {
		URI url = buildUri( path, queryParams );
		HttpEntity<?> request = new HttpEntity<MultiValueMap<?, ?>>( new LinkedMultiValueMap<String, String>() );
		JsonResponse<T> body = null;
		try {
			ResponseEntity<JsonResponse<T>> response =
					restTemplate.exchange( url, HttpMethod.GET, request, responseType );

			body = response.getBody();

		} catch (Exception e){
			LOG.error( e.getMessage() );
			throw new ImageServerException( e.getMessage() );
		}

		if ( !body.isSuccess() ) {
			throw new ImageServerException( body.getErrorMessage() );
		}

		return body.getResult();
	}

	protected <T> T httpPost( String path,
	                          MultiValueMap<String, String> queryParams,
	                          MultiValueMap<String, Object> bodyParams,
	                          ParameterizedTypeReference<JsonResponse<T>> responseType ) {
		URI url = buildUri( path, queryParams );

		HttpEntity<?> request =
				new HttpEntity<MultiValueMap<?, ?>>( bodyParams );

		ResponseEntity<JsonResponse<T>> response =
				restTemplate.exchange( url, HttpMethod.POST, request, responseType );

		JsonResponse<T> body = response.getBody();

		if ( !body.isSuccess() ) {
			throw new ImageServerException( body.getErrorMessage() );
		}

		return body.getResult();
	}
}
