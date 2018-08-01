package com.foreach.imageserver.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class ImageModificationDto
{
	/**
	 * -- GETTER --
	 *
	 * The id of the resolution whose modification was used as the base for building this one.
	 * If this modification is registered directly to the resolution, this id will be the same as
	 * the id property of {@link #getResolution()}.  If another modification was used as base but then
	 * translated to match the requested resolution, then this property should return the id of
	 * the original modification, not the result of the translation.
	 *
	 * @return id of the resolution or {@code null} if none
	 */
	private Long baseResolutionId;
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

	public boolean hasCrop() {
		return crop != null && !crop.equals( new CropDto() );
	}

	public boolean hasBoundaries() {
		return boundaries != null && !boundaries.equals( new DimensionsDto() );
	}

	/**
	 * Returns {@code true} if this modification is registered for the resolution it actually returns
	 * This will be so if {@link #getBaseResolutionId()} equals the id property of {@link #getResolution()}.
	 *
	 * @return {@code true} if this modification is registered for the resolution it returns
	 */
	public boolean isRegistered() {
		return baseResolutionId != null && resolution != null && baseResolutionId.equals( resolution.getId() );
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
