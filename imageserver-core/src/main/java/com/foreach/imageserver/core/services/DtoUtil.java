package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.dto.CropDto;
import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageResolutionDto;

import java.util.ArrayList;
import java.util.List;

// TODO: review to simply business and dto objects layer
public final class DtoUtil
{

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

	public static List<ImageResolutionDto> toDto( List<ImageResolution> imageResolutions ) {
		List<ImageResolutionDto> dtos = new ArrayList<>( imageResolutions.size() );
		for ( ImageResolution imageResolution : imageResolutions ) {
			ImageResolutionDto dto = DtoUtil.toDto( imageResolution );
			dtos.add( dto );
		}
		return dtos;
	}
}
