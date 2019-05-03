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
import java.util.Map;
import java.util.UUID;

import static org.springframework.util.Assert.notNull;

/**
 * Represents a client for a remote ImageServer endpoint.
 */
public class RemoteImageServerClient extends AbstractImageServerClient
{
	private static final Logger LOG = LoggerFactory.getLogger( RemoteImageServerClient.class );
	private final String imageServerAccessToken;
	private RestTemplate restTemplate;

	public RemoteImageServerClient( String imageServerEndpoint, String imageServerAccessToken ) {
		this( imageServerEndpoint, imageServerAccessToken, new RestTemplate() );
	}

	public RemoteImageServerClient( String imageServerEndpoint, String imageServerAccessToken, RestTemplate restTemplate ) {
		super( imageServerEndpoint );

		this.imageServerAccessToken = imageServerAccessToken;

		this.restTemplate = restTemplate;
	}

	public void setRestTemplate( RestTemplate restTemplate ) {
		notNull( restTemplate, "RestTemplate can not be null" );
		this.restTemplate = restTemplate;
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

		hashBuilder().ifPresent(
				hashBuilder -> queryParams.set(
						"hash", hashBuilder.calculateHash( context, null, imageResolution, imageVariant )
				)
		);

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
	public InputStream imageStream( byte[] imageData,
	                                ImageModificationDto imageModificationDto,
	                                ImageVariantDto imageVariant ) {
		if ( imageData == null || imageModificationDto == null || imageVariant == null ) {
			LOG.warn(
					"Null parameters not allowed - ImageServerClientImpl#imageStream: imageData={}, imageModificationDto={}, imageResolution={}, imageVariant={}",
					LogHelper.flatten( imageData == null, imageModificationDto, imageVariant ) );
		}

		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.set( "token", imageServerAccessToken );
		addQueryParams( queryParams, imageModificationDto );
		addQueryParams( queryParams, imageVariant );

		MultiValueMap<String, Object> bodyParts = getImageDataAsBody( imageData );

		return new ByteArrayInputStream( httpPost( ENDPOINT_IMAGE_RENDER, queryParams, bodyParts, byte[].class ) );
	}

	@Override
	public ImageInfoDto loadImage( String imageId, byte[] imageBytes ) {
		return loadImage( imageId, imageBytes, false );
	}

	@Override
	public ImageInfoDto loadImage( String imageId, byte[] imageBytes, boolean replaceExisting ) {
		return loadImage( imageId, imageBytes, null, replaceExisting );
	}

	@Override
	public ImageInfoDto loadImage( String imageId, byte[] imageBytes, Date imageDate ) {
		return loadImage( imageId, imageBytes, imageDate, false );
	}

	@Override
	public ImageInfoDto loadImage( String imageId, byte[] imageBytes, Date imageDate, boolean replaceExisting ) {
		if ( StringUtils.isBlank( imageId ) ) {
			throw new ImageServerException( "You must specify an imageId when loading an image." );
		}
		if ( imageBytes == null || imageBytes.length == 0 ) {
			throw new ImageServerException( "Unable to load an image with empty byte data." );
		}

		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.set( "token", imageServerAccessToken );
		queryParams.set( "iid", imageId );
		queryParams.set( "replaceExisting", Boolean.toString( replaceExisting ) );
		if ( imageDate != null ) {
			queryParams.set( "imageTimestamp", Long.toString( imageDate.getTime() ) );
		}

		MultiValueMap<String, Object> bodyParts = getImageDataAsBody( imageBytes );

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

	private MultiValueMap<String, Object> getImageDataAsBody( final byte[] imageBytes ) {
		MultiValueMap<String, Object> bodyParts = new LinkedMultiValueMap<>();
		bodyParts.set( "imageData", new ByteArrayResource( imageBytes )
		{
			@Override
			public String getFilename() {
				return "imageData";
			}
		} );
		return bodyParts;
	}

	@Override
	public boolean deleteImage( String imageId ) {
		if ( StringUtils.isBlank( imageId ) ) {
			throw new ImageServerException( "You must specify an imageId when deleting an image." );
		}

		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.set( "token", imageServerAccessToken );
		queryParams.set( "iid", imageId );

		Object map = httpPost( ENDPOINT_IMAGE_DELETE, queryParams, new LinkedMultiValueMap<String, String>(),
		                       ResponseTypes.OBJECT );

		if ( map instanceof Map ) {
			return Boolean.valueOf( "" + ( (Map) map ).get( "deleted" ) );
		}

		return false;
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
	public ImageInfoDto imageInfo( byte[] imageBytes ) {
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.set( "token", imageServerAccessToken );

		MultiValueMap<String, Object> bodyParts = getImageDataAsBody( imageBytes );

		return httpPost( ENDPOINT_IMAGE_INFO, queryParams, bodyParts, ResponseTypes.IMAGE_INFO );
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

		MultiValueMap<String, String> bodyParts = new LinkedMultiValueMap<>();
		addQueryParams( bodyParts, imageModifications );

		httpPost( ENDPOINT_MODIFICATION_REGISTER_LIST, queryParams, bodyParts, ResponseTypes.OBJECT );
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

	@Override
	public ImageConvertResultDto convertImage( ImageConvertDto convertDto ) {
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.set( "token", imageServerAccessToken );

		return httpPost( ENDPOINT_IMAGE_CONVERT, queryParams, new HttpEntity<>( convertDto ), ResponseTypes.IMAGE_CONVERT );
	}

	@Override
	public ImageConvertResultTransformationDto convertImage( byte[] imageBytes, List<ImageTransformDto> transforms ) {
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.set( "token", imageServerAccessToken );

		String key = UUID.randomUUID().toString();

		ImageConvertDto convertDto = ImageConvertDto.builder()
		                                            .image( imageBytes )
		                                            .transformation( key, transforms )
		                                            .build();

		return httpPost( ENDPOINT_IMAGE_CONVERT, queryParams, new HttpEntity<>( convertDto ), ResponseTypes.IMAGE_CONVERT )
				.getTransforms()
				.get( key );
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
		JsonResponse<T> body;
		try {
			ResponseEntity<JsonResponse<T>> response =
					restTemplate.exchange( url, HttpMethod.GET, request, responseType );

			body = response.getBody();

		}
		catch ( Exception e ) {
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
	                          MultiValueMap<String, ?> bodyParams,
	                          Class<T> responseType ) {
		URI url = buildUri( path, queryParams );

		HttpEntity<?> request =
				new HttpEntity<MultiValueMap<?, ?>>( bodyParams );

		ResponseEntity<T> response = restTemplate.exchange( url, HttpMethod.POST, request, responseType );

		return response.getBody();
	}

	protected <T> T httpPost( String path,
	                          MultiValueMap<String, String> queryParams,
	                          HttpEntity payload,
	                          ParameterizedTypeReference<JsonResponse<T>> responseType ) {
		URI url = buildUri( path, queryParams );

		ResponseEntity<JsonResponse<T>> response = restTemplate.exchange( url, HttpMethod.POST, payload, responseType );

		JsonResponse<T> body = response.getBody();

		return body.getResult();
	}

	protected <T> T httpPost( String path,
	                          MultiValueMap<String, String> queryParams,
	                          MultiValueMap<String, ?> bodyParams,
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

	private static final class ResponseTypes
	{
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
		private static final ParameterizedTypeReference<JsonResponse<ImageConvertResultDto>> IMAGE_CONVERT =
				new ParameterizedTypeReference<JsonResponse<ImageConvertResultDto>>()
				{
				};

		private ResponseTypes() {
		}
	}
}
