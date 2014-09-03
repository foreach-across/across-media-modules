package com.foreach.imageserver.dto;

import java.util.Objects;

public class ImageModificationDto
{
	private ImageResolutionDto resolution;
	private CropDto crop;
	private DimensionsDto density;
	private DimensionsDto boundaries;

	public ImageModificationDto() {
		this.resolution = new ImageResolutionDto();
		this.crop = new CropDto();
		this.density = new DimensionsDto();
		this.boundaries = new DimensionsDto();
	}

	public ImageModificationDto( ImageModificationDto original ) {
		this.resolution = new ImageResolutionDto( original.getResolution() );
		this.crop = new CropDto( original.getCrop() );
		this.density = new DimensionsDto( original.getDensity() );
		this.boundaries = new DimensionsDto( original.getBoundaries() );
	}

	public ImageModificationDto( int width, int height ) {
		this();

		resolution.setWidth( width );
		resolution.setHeight( height );
	}

	public ImageModificationDto( ImageResolutionDto resolution, CropDto crop, DimensionsDto density ) {
		this( resolution, crop, density, new DimensionsDto() );
	}

	public ImageModificationDto( ImageResolutionDto resolution,
	                             CropDto crop,
	                             DimensionsDto density,
	                             DimensionsDto boundaries ) {
		this.resolution = resolution;
		this.crop = crop;
		this.density = density;
		this.boundaries = boundaries;
	}

	public ImageResolutionDto getResolution() {
		return resolution;
	}

	public void setResolution( ImageResolutionDto resolution ) {
		this.resolution = resolution;
	}

	public CropDto getCrop() {
		return crop;
	}

	public void setCrop( CropDto crop ) {
		this.crop = crop;
	}

	public DimensionsDto getDensity() {
		return density;
	}

	public void setDensity( DimensionsDto density ) {
		this.density = density;
	}

	public DimensionsDto getBoundaries() {
		return boundaries;
	}

	public void setBoundaries( DimensionsDto boundaries ) {
		this.boundaries = boundaries;
	}

	public boolean hasCrop() {
		return crop != null && !crop.equals( new CropDto() );
	}

	public boolean hasBoundaries() {
		return boundaries != null && !boundaries.equals( new DimensionsDto() );
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( !( o instanceof ImageModificationDto ) ) {
			return false;
		}

		ImageModificationDto that = (ImageModificationDto) o;

		return Objects.equals( boundaries, that.boundaries )
				&& Objects.equals( crop, that.crop )
				&& Objects.equals( density, that.density )
				&& Objects.equals( resolution, that.resolution );
	}

	@Override
	public int hashCode() {
		return Objects.hash( resolution, crop, density, boundaries );
	}
}
