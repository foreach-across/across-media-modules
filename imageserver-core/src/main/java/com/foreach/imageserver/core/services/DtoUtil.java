package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.dto.*;
import com.foreach.imageserver.math.AspectRatio;

import java.util.*;

// TODO: review to simply business and dto objects layer
public final class DtoUtil
{
	private DtoUtil() {
	}

	public static AspectRatio toBusiness( ImageAspectRatioDto imageAspectRatioDto ) {
		return new AspectRatio( imageAspectRatioDto.getRatio() );
	}

	public static Crop toBusiness( CropDto dto ) {
		Crop crop = new Crop();
		crop.setX( dto.getX() );
		crop.setY( dto.getY() );
		crop.setWidth( dto.getWidth() );
		crop.setHeight( dto.getHeight() );
		return crop;
	}

	public static ImageResolution toBusiness( ImageResolutionDto dto ) {
		ImageResolution resolution = new ImageResolution();
		resolution.setId( dto.getId() );
		resolution.setWidth( dto.getWidth() );
		resolution.setHeight( dto.getHeight() );
		resolution.setPregenerateVariants( dto.isPregenerateVariants() );
		resolution.setConfigurable( dto.isConfigurable() );
		resolution.setName( dto.getName() );
		resolution.setTags( dto.getTags() );
		resolution.setAllowedOutputTypes( toBusiness( dto.getAllowedOutputTypes() ) );
		return resolution;
	}

	public static Dimensions toBusiness( DimensionsDto dto ) {
		Dimensions dimensions = new Dimensions();
		dimensions.setWidth( dto.getWidth() );
		dimensions.setHeight( dto.getHeight() );
		return dimensions;
	}

	public static ImageVariant toBusiness( ImageVariantDto dto ) {
		ImageVariant imageVariant = new ImageVariant();
		imageVariant.setOutputType( toBusiness( dto.getImageType() ) );
		if ( dto.getBoundaries() != null ) {
			imageVariant.setBoundaries( toBusiness( dto.getBoundaries() ) );
		}
		return imageVariant;
	}

	public static Set<ImageType> toBusiness( Set<ImageTypeDto> dtoSet ) {
		Set<ImageType> imageTypes = EnumSet.noneOf( ImageType.class );

		for ( ImageTypeDto dto : dtoSet ) {
			ImageType imageType = toBusiness( dto );
			if ( imageType != null ) {
				imageTypes.add( imageType );
			}
		}
		return imageTypes;

	}

	public static ImageType toBusiness( ImageTypeDto dto ) {
		if ( dto == null ) {
			return null;
		}
		switch ( dto ) {
			case JPEG:
				return ImageType.JPEG;
			case PNG:
				return ImageType.PNG;
			case GIF:
				return ImageType.GIF;
			case SVG:
				return ImageType.SVG;
			case EPS:
				return ImageType.EPS;
			case PDF:
				return ImageType.PDF;
			case TIFF:
				return ImageType.TIFF;
			case BMP:
				return ImageType.BMP;
			default:
				return null;
		}
	}

	public static ImageAspectRatioDto toDto( AspectRatio aspectRatio ) {
		return new ImageAspectRatioDto( aspectRatio.toString() );
	}

	public static ImageContextDto toDto( ImageContext context ) {
		ImageContextDto contextDto = new ImageContextDto();
		contextDto.setId( context.getId() );
		contextDto.setCode( context.getCode() );

		return contextDto;
	}

	public static ImageInfoDto toDto( Image image ) {
		ImageInfoDto dto = new ImageInfoDto();
		dto.setExisting( true );
		dto.setExternalId( image.getExternalId() );
		dto.setCreated( image.getDateCreated() );
		dto.setDimensionsDto( toDto( image.getDimensions() ) );
		dto.setImageType( toDto( image.getImageType() ) );
		dto.setImageFileSize( image.getFileSize() );
		dto.setSceneCount( image.getSceneCount() );

		return dto;
	}

	public static DimensionsDto toDto( Dimensions dimensions ) {
		return new DimensionsDto( dimensions.getWidth(), dimensions.getHeight() );
	}

	public static CropDto toDto( Crop crop ) {
		return new CropDto( crop.getX(), crop.getY(), crop.getWidth(), crop.getHeight() );
	}

	public static List<ImageResolutionDto> toDto( Collection<ImageResolution> imageResolutions ) {
		List<ImageResolutionDto> dtos = new ArrayList<>( imageResolutions.size() );
		for ( ImageResolution imageResolution : imageResolutions ) {
			ImageResolutionDto dto = DtoUtil.toDto( imageResolution );
			dtos.add( dto );
		}
		return dtos;
	}

	public static ImageResolutionDto toDto( ImageResolution imageResolution ) {
		ImageResolutionDto dto = new ImageResolutionDto();
		dto.setId( imageResolution.getId() );
		dto.setWidth( imageResolution.getWidth() );
		dto.setHeight( imageResolution.getHeight() );
		dto.setPregenerateVariants( imageResolution.isPregenerateVariants() );
		dto.setAllowedOutputTypes( toDto( imageResolution.getAllowedOutputTypes() ) );
		dto.setConfigurable( imageResolution.isConfigurable() );
		dto.setName( imageResolution.getName() );
		dto.setTags( imageResolution.getTags() );

		return dto;
	}

	public static Set<ImageTypeDto> toDto( Set<ImageType> imageTypeSet ) {
		Set<ImageTypeDto> imageTypes = EnumSet.noneOf( ImageTypeDto.class );

		for ( ImageType imageType : imageTypeSet ) {
			ImageTypeDto dto = toDto( imageType );
			if ( dto != null ) {
				imageTypes.add( dto );
			}
		}

		return imageTypes;
	}

	public static ImageTypeDto toDto( ImageType imageType ) {
		return ImageTypeDto.valueOf( imageType.name() );
	}

}
