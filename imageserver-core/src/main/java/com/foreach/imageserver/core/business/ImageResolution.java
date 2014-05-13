package com.foreach.imageserver.core.business;

/**
 * <p>An ImageResolution specifies a permitted output resolution. Every Application has an associated list of
 * ImageResolution-s for which ImageModification-s can be registered.</p>
 * <p>
 * Note that width and height are nullable. When a dimension is set explicitly, associated ImageModification-s should
 * adhere to it exactly. When a dimension is NULL, however, we expect the ImageModification to vary it so that the
 * aspect ratio of the original image is maintained.</p>
 * <p>
 * For specifying the actual dimensions of an image, see Dimensions.
 * </p>
 * <p>Configurable means that a crop can be configured explicitly for the resolution.  A non-configurable resolution
 * can still be requested, but will not be offered for manual crop configuration.</p>
 * <p>ImageResolution name is optional and can be used to provide a more meaningful description to a (mostly
 * configurable) resolution, eg. Large teaser format.</p>
 */
public class ImageResolution {
    private Integer id;
    private Integer width;
    private Integer height;

    private boolean configurable;
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public boolean isConfigurable() {
        return configurable;
    }

    public void setConfigurable(boolean configurable) {
        this.configurable = configurable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }
}
