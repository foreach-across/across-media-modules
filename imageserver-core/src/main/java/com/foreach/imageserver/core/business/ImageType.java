package com.foreach.imageserver.core.business;

import com.foreach.across.modules.hibernate.types.BitFlag;
import com.foreach.common.spring.enums.IdLookup;
import org.apache.commons.lang3.StringUtils;

public enum ImageType implements IdLookup<Integer>, BitFlag
{
	JPEG( 1, "image/jpeg", "jpeg", false, false ),
	PNG( 2, "image/png", "png", true, false ),
	GIF( 4, "image/gif", "gif", true, false ),
	SVG( 8, "image/svg+xml", "svg", true, true ),
	EPS( 16, "application/postscript", "eps", true, true, "image/eps", "image/x-eps",
	     "application/eps", "application/x-eps" ),
	PDF( 32, "application/pdf", "pdf", false, false, "application/x-pdf" ),
	TIFF( 64, "image/tiff", "tif", false, false );

	private final int id;
	private final String contentType, extension;
	private final String[] alternativeContentTypes;
	private final boolean transparency, scalable;

	ImageType( int id,
	                   String contentType,
	                   String extension,
	                   boolean transparency,
	                   boolean scalable,
	                   String... alternativeContentTypes ) {
		this.id = id;
		this.contentType = contentType;
		this.extension = extension;
		this.alternativeContentTypes = alternativeContentTypes;
		this.transparency = transparency;
		this.scalable = scalable;
	}


	public String getContentType() {
		return contentType;
	}

	public String getExtension() {
		return extension;
	}

	public boolean hasTransparency() {
		return transparency;
	}

	public boolean isScalable() {
		return scalable;
	}

	@Override
	public Integer getId() {
		return id;
	}

	public static ImageType getForContentType( String contentType ) {
		for ( ImageType imageType : values() ) {
			if ( StringUtils.equalsIgnoreCase( imageType.getContentType(), contentType ) ) {
				return imageType;
			}
			for ( String alternative : imageType.alternativeContentTypes ) {
				if ( StringUtils.equalsIgnoreCase( alternative, contentType ) ) {
					return imageType;
				}
			}
		}
		return null;
	}

	public static ImageType getForExtension( String uri ) {
		for ( ImageType imageType : values() ) {
			if ( StringUtils.endsWithIgnoreCase( uri, "." + imageType.getExtension() ) ) {
				return imageType;
			}
		}
		return null;
	}

	public static ImageType getPreferredOutputType( ImageType requestedImageType, ImageType originalImageType ) {
		if ( requestedImageType != null ) {
			return requestedImageType;
		}
		switch ( originalImageType ) {
			case GIF:
				return GIF;
			case SVG:
			case EPS:
			case PNG:
				return PNG;
			default:
				return JPEG;
		}
	}

	@Override
	public int getBitFlag() {
		return id;
	}
}
