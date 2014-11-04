package com.foreach.imageserver.core.rest.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.rest.request.ListModificationsRequest;
import com.foreach.imageserver.core.rest.request.ListResolutionsRequest;
import com.foreach.imageserver.core.rest.request.RegisterModificationRequest;
import com.foreach.imageserver.core.rest.request.ViewImageRequest;
import com.foreach.imageserver.core.rest.response.*;
import com.foreach.imageserver.core.services.CropGeneratorUtil;
import com.foreach.imageserver.core.services.DtoUtil;
import com.foreach.imageserver.core.services.ImageContextService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.services.exceptions.CropOutsideOfImageBoundsException;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.dto.ImageVariantDto;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Arne Vandamme
 */
@Service
public class ImageRestServiceImpl implements ImageRestService
{
	private static final Logger LOG = LoggerFactory.getLogger( ImageRestService.class );

	// explicit logging of requested images that do not exist
	private static final Logger LOG_IMAGE_NOT_FOUND = LoggerFactory.getLogger( Image.class );

	@Autowired
	private ImageContextService contextService;

	@Autowired
	private CropGeneratorUtil cropGeneratorUtil;

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
		} else {
			StreamImageSource imageSource = imageService.generateModification(
					image,
					request.getImageModificationDto(),
					imageVariant( image, request.getImageVariantDto() )
			);

			if ( imageSource == null ) {
				response.setFailed( true );
			} else {
				response.setImageSource( imageSource );
			}
		}

		return response;
	}

	@Override
	public PregenerateResolutionsResponse pregenerateResolutions( String imageId ) {
		PregenerateResolutionsResponse response = new PregenerateResolutionsResponse();

		final Image image = imageService.getByExternalId( imageId );

		if ( image == null ) {
			response.setImageDoesNotExist( true );
		} else {
			final List<ImageResolution> pregenerateList = new LinkedList<>();
			Collection<ImageResolution> resolutions = imageService.getAllResolutions();

			for ( ImageResolution resolution : resolutions ) {
				if ( resolution.isPregenerateVariants() ) {
					pregenerateList.add( resolution );
				}
			}

			if ( !pregenerateList.isEmpty() ) {
				response.setImageResolutions( DtoUtil.toDto( pregenerateList ) );

				// TODO: offload this as set of tasks to generation service (with a threadpool)
				Runnable runnable = new Runnable()
				{
					@Override
					public void run() {
						LOG.debug( "Start pregeneration of {} resolutions for {}", pregenerateList.size(), image );
						for ( final ImageResolution resolution : pregenerateList ) {
							List<ImageType> allowedTypes = Arrays.asList( ImageType.JPEG );
							for ( ImageType outputType : allowedTypes ) {
								ImageVariant variant = new ImageVariant();
								variant.setOutputType( outputType );

								for ( ImageContext context : resolution.getContexts() ) {
									try {
										imageService.getVariantImage( image, context, resolution, variant );

										LOG.info(
												"Finished pregenerating resolution {} for {}: context {} - imageType {}",
												resolution, image, context.getCode(), outputType );
									}
									catch ( Exception e ) {
										LOG.warn(
												"Problem pregenerating resolution {} for {}: context {} - imageType {}",
												resolution, image, context.getCode(), outputType, e );
									}
								}
							}
						}

						LOG.info( "Finished pregenerating {} resolutions for {}", pregenerateList.size(), image );
					}
				};

				new Thread( runnable ).start();
			}
		}

		return response;
	}

	@Override
	public ViewImageResponse viewImage( ViewImageRequest request ) {
		ViewImageResponse response = new ViewImageResponse( request );

		Image image = imageService.getByExternalId( request.getExternalId() );
		if ( image == null ) {
			LOG_IMAGE_NOT_FOUND.error( request.getExternalId() );
		}

		if ( image == null
				&& StringUtils.isNotBlank( fallbackImageKey )
				&& !StringUtils.equals( request.getExternalId(), fallbackImageKey ) ) {
			image = imageService.getByExternalId( fallbackImageKey );
		}

		if ( image == null ) {
			response.setImageDoesNotExist( true );
		} else {
			ImageContext context = contextService.getByCode( request.getContext() );

			if ( context == null ) {
				response.setContextDoesNotExist( true );
			} else {
				ImageResolutionDto imageResolutionDto = request.getImageResolutionDto();
				ImageResolution imageResolution = contextService.getImageResolution(
						context.getId(), imageResolutionDto.getWidth(), imageResolutionDto.getHeight()
				);

				if ( imageResolution == null ) {
					LOG.warn( "Resolution {}x{} does not exist for context {}", imageResolutionDto.getWidth(),
					          imageResolutionDto.getHeight(), context.getCode() );
					response.setResolutionDoesNotExist( true );
				} else {
					// when available, the bounding box dimensions should be those of an existing resolution
					DimensionsDto boundaries = request.getImageVariantDto().getBoundaries();
					if ( boundaries != null && !boundingResolutionExists( boundaries, context ) ) {
						LOG.warn( "Bounding box resolution {}x{} does not exist for context {}", imageResolutionDto.getWidth(),
						          imageResolutionDto.getHeight(), context.getCode() );
						response.setResolutionDoesNotExist( true );
					} else {

						ImageVariant variant = imageVariant( image, request.getImageVariantDto() );

						if ( !imageResolution.isAllowedOutputType( variant.getOutputType() ) ) {
							LOG.warn( "Output type {} is not allowed for resolution {}", variant.getOutputType(),
							          imageResolution );

							response.setOutputTypeNotAllowed( true );
						} else {
							StreamImageSource imageSource = imageService.getVariantImage(
									image, context, imageResolution, variant
							);

							if ( imageSource == null ) {
								response.setFailed( true );
							} else {
								response.setImageSource( imageSource );
							}
						}
					}
				}
			}
		}

		return response;
	}

	private boolean boundingResolutionExists( DimensionsDto boundaries, ImageContext context ) {
		if ( boundaries != null ) {
			ImageResolution boundingRsolution = contextService.getImageResolution(
					context.getId(), boundaries.getWidth(), boundaries.getHeight()
			);
			return boundingRsolution != null;
		}
		return false;
	}

	private ImageVariant imageVariant( Image image, ImageVariantDto variantDto ) {
		ImageVariant variant = DtoUtil.toBusiness( variantDto );

		// Ensure an output image type is set
		variant.setOutputType( ImageType.getPreferredOutputType( variant.getOutputType(), image.getImageType() ) );

		return variant;
	}

	@Override
	public ListModificationsResponse listModifications( ListModificationsRequest request ) {
		ListModificationsResponse response = new ListModificationsResponse( request );

		ImageContext context = contextService.getByCode( request.getContext() );

		if ( context == null ) {
			response.setContextDoesNotExist( true );
		} else {
			Image image = imageService.getByExternalId( request.getExternalId() );
			if ( image == null ) {
				response.setImageDoesNotExist( true );
			} else {
				List<ImageModification> modifications = imageService.getModifications( image.getId(), context.getId() );
				response.setModifications( toModificationDtos( modifications ) );
			}
		}

		return response;
	}

	private List<ImageModificationDto> toModificationDtos( List<ImageModification> modifications ) {
		List<ImageModificationDto> dtos = new ArrayList<>( modifications.size() );
		for ( ImageModification modification : modifications ) {
			dtos.add( toDto( modification ) );
		}
		return dtos;
	}

	private ImageModificationDto toDto( ImageModification modification ) {
		ImageResolution resolution = imageService.getResolution( modification.getResolutionId() );
		ImageModificationDto dto = new ImageModificationDto();
		dto.getResolution().setWidth( resolution.getWidth() );
		dto.getResolution().setHeight( resolution.getHeight() );
		dto.getCrop().setX( modification.getCrop().getX() );
		dto.getCrop().setY( modification.getCrop().getY() );
		dto.getCrop().setWidth( modification.getCrop().getWidth() );
		dto.getCrop().setHeight( modification.getCrop().getHeight() );
		dto.getDensity().setWidth( modification.getDensity().getWidth() );
		dto.getDensity().setHeight( modification.getDensity().getHeight() );
		return dto;
	}

	@Override
	public RegisterModificationResponse registerModifications( RegisterModificationRequest request ) {
		RegisterModificationResponse response = new RegisterModificationResponse( request );

		ImageContext context = contextService.getByCode( request.getContext() );

		if ( context == null ) {
			response.setContextDoesNotExist( true );
		} else {
			Image image = imageService.getByExternalId( request.getExternalId() );
			if ( image == null ) {
				response.setImageDoesNotExist( true );
			} else {
				List<ImageModification> modifications = extractImageModifications( request, image, context, response );
				imageService.saveImageModifications( modifications, image );
			}
		}

		return response;
	}

	private List<ImageModification> extractImageModifications( RegisterModificationRequest request,
	                                                           Image image,
	                                                           ImageContext context,
	                                                           RegisterModificationResponse response ) {
		List<ImageModification> modifications = new ArrayList<ImageModification>();

		if ( request.getImageModificationDto() != null ) {
			modifications.add( toModification( request.getImageModificationDto(), image, context, response ) );
		}
		if ( request.getImageModificationDtos() != null ) {
			for ( ImageModificationDto modificationDto : request.getImageModificationDtos() ) {
				modifications.add( toModification( modificationDto, image, context, response ) );

			}
		}

		return modifications;
	}

	private ImageModification toModification( ImageModificationDto imageModificationDto,
	                                          Image image,
	                                          ImageContext context,
	                                          RegisterModificationResponse response ) {
		ImageModification modification = null;

		ImageResolutionDto resolutionDto = imageModificationDto.getResolution();

		ImageResolution imageResolution =
				contextService.getImageResolution( context.getId(),
				                                   resolutionDto.getWidth(),
				                                   resolutionDto.getHeight() );

		if ( imageResolution == null ) {
			response.addMissingResolution( resolutionDto );
		} else {
			cropGeneratorUtil.normalizeModificationDto( image, imageModificationDto );

			modification = new ImageModification();
			modification.setImageId( image.getId() );
			modification.setContextId( context.getId() );
			modification.setResolutionId( imageResolution.getId() );
			modification.setCrop( DtoUtil.toBusiness( imageModificationDto.getCrop() ) );
			modification.setDensity( DtoUtil.toBusiness( imageModificationDto.getDensity() ) );

			try {
				imageService.saveImageModification( modification, image );
			}
			catch ( CropOutsideOfImageBoundsException e ) {
				response.setCropOutsideOfImageBounds( true );
			}
		}

		return modification;
	}

	@Override
	public ListResolutionsResponse listResolutions( ListResolutionsRequest request ) {
		ListResolutionsResponse response = new ListResolutionsResponse( request );

		Collection<ImageResolution> imageResolutions = null;

		if ( StringUtils.isNotBlank( request.getContext() ) ) {
			ImageContext context = contextService.getByCode( request.getContext() );

			if ( context == null ) {
				response.setContextDoesNotExist( true );
			} else {
				imageResolutions = contextService.getImageResolutions( context.getId() );
			}
		} else {
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
