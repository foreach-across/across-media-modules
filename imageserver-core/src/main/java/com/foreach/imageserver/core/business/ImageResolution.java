package com.foreach.imageserver.core.business;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

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
public class ImageResolution
{
	private Integer id;
	private int width;
	private int height;

	private boolean configurable;
	private String name;
	private Set<String> tags = new HashSet<>();

	public Integer getId() {
		return id;
	}

	public void setId( Integer id ) {
		this.id = id;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth( int width ) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight( int height ) {
		this.height = height;
	}

	public Dimensions getDimensions() {
		return new Dimensions( getWidth(), getHeight() );
	}

	public boolean isConfigurable() {
		return configurable;
	}

	public void setConfigurable( boolean configurable ) {
		this.configurable = configurable;
	}

	public String getName() {
		return StringUtils.isBlank( name ) ? generatedName() : name;
	}

	private String generatedName() {

		if ( width == 0 && height == 0 ) {
			return "original";
		}
		else if ( width == 0 ) {
			return "H" + height;
		}
		else if ( height == 0 ) {
			return "W" + width;
		}
		else {
			return width + "x" + height;
		}
	}

	public void setName( String name ) {
		this.name = name;
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags( Set<String> tags ) {
		this.tags = tags;
	}

	@Override
	public String toString() {
		return width + "x" + height;
	}
}
