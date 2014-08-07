package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.dto.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// TODO: review to simply business and dto objects layer
public final class DtoUtil
{
	private DtoUtil() {}
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
		resolution.setConfigurable( dto.isConfigurable() );
		resolution.setName( dto.getName() );
		resolution.setTags( dto.getTags() );
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
		return imageVariant;
	}

	public static ImageType toBusiness( ImageTypeDto dto ) {
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
			default:
				throw new RuntimeException( "Unknown image type." );
		}
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
		dto.setImageType( ImageTypeDto.valueOf( image.getImageType().name() ) );
		dto.setImageFileSize( image.getFileSize() );

		return dto;
	}

	public static DimensionsDto toDto( Dimensions dimensions ) {
		return new DimensionsDto( dimensions.getWidth(), dimensions.getHeight() );
	}

	public static CropDto toDto( Crop crop ) {
		return new CropDto( crop.getX(), crop.getY(), crop.getWidth(), crop.getHeight() );
	}

	public static ImageResolutionDto toDto( ImageResolution imageResolution ) {
		ImageResolutionDto dto = new ImageResolutionDto();
		dto.setId( imageResolution.getId() );
		dto.setWidth( imageResolution.getWidth() );
		dto.setHeight( imageResolution.getHeight() );
		dto.setConfigurable( imageResolution.isConfigurable() );
		dto.setName( imageResolution.getName() );
		dto.setTags( imageResolution.getTags() );

		return dto;
	}

	public static List<ImageResolutionDto> toDto( Collection<ImageResolution> imageResolutions ) {
		List<ImageResolutionDto> dtos = new ArrayList<>( imageResolutions.size() );
		for ( ImageResolution imageResolution : imageResolutions ) {
			ImageResolutionDto dto = DtoUtil.toDto( imageResolution );
			dtos.add( dto );
		}
		return dtos;
	}
}
