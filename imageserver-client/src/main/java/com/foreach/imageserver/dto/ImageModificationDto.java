package com.foreach.imageserver.dto;

public class ImageModificationDto {
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
        this.resolution = new ImageResolutionDto( original.getResolution());
        this.crop = new CropDto( original.getCrop() );
        this.density = new DimensionsDto( original.getDensity() );
        this.boundaries = new DimensionsDto( original.getBoundaries());
    }

    public ImageModificationDto(int width, int height) {
        this();

        resolution.setWidth(width);
        resolution.setHeight(height);
    }

    public ImageModificationDto(ImageResolutionDto resolution, CropDto crop, DimensionsDto density) {
        this(resolution, crop, density, new DimensionsDto());
    }

    public ImageModificationDto(ImageResolutionDto resolution, CropDto crop, DimensionsDto density, DimensionsDto boundaries) {
        this.resolution = resolution;
        this.crop = crop;
        this.density = density;
        this.boundaries = boundaries;
    }

    public ImageResolutionDto getResolution() {
        return resolution;
    }

    public void setResolution(ImageResolutionDto resolution) {
        this.resolution = resolution;
    }

    public CropDto getCrop() {
        return crop;
    }

    public void setCrop(CropDto crop) {
        this.crop = crop;
    }

    public DimensionsDto getDensity() {
        return density;
    }

    public void setDensity(DimensionsDto density) {
        this.density = density;
    }

    public DimensionsDto getBoundaries() {
        return boundaries;
    }

    public void setBoundaries(DimensionsDto boundaries) {
        this.boundaries = boundaries;
    }

    public boolean hasCrop() {
        return crop != null && !crop.equals(new CropDto());
    }

    public boolean hasBoundaries() {
        return boundaries != null && !boundaries.equals(new DimensionsDto());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageModificationDto)) return false;

        ImageModificationDto that = (ImageModificationDto) o;

        if (boundaries != null ? !boundaries.equals(that.boundaries) : that.boundaries != null) return false;
        if (crop != null ? !crop.equals(that.crop) : that.crop != null) return false;
        if (density != null ? !density.equals(that.density) : that.density != null) return false;
        if (resolution != null ? !resolution.equals(that.resolution) : that.resolution != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = resolution != null ? resolution.hashCode() : 0;
        result = 31 * result + (crop != null ? crop.hashCode() : 0);
        result = 31 * result + (density != null ? density.hashCode() : 0);
        result = 31 * result + (boundaries != null ? boundaries.hashCode() : 0);
        return result;
    }
}
