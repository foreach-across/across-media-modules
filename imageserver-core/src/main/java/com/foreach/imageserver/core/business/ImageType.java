package com.foreach.imageserver.core.business;

import com.foreach.spring.enums.IdLookup;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

public enum ImageType implements IdLookup<BigDecimal>
{
	JPEG( BigDecimal.valueOf( 0 ), "image/jpeg", "jpeg", false, false ),
	PNG( BigDecimal.valueOf( 1 ), "image/png", "png", true, false ),
	GIF( BigDecimal.valueOf( 2 ), "image/gif", "gif", true, false ),
	SVG( BigDecimal.valueOf( 3 ), "image/svg+xml", "svg", true, true ),
	EPS( BigDecimal.valueOf( 4 ), "application/postscript", "eps", true, true, "image/eps", "image/x-eps",
	     "application/eps", "application/x-eps" ),
	PDF( BigDecimal.valueOf( 5 ), "application/pdf", "pdf", false, false, "application/x-pdf" ),
	TIFF( BigDecimal.valueOf( 6 ), "image/tiff", "tif", false, false );

	private final BigDecimal id;
	private final String contentType, extension;
	private final String[] alternativeContentTypes;
	private final boolean transparency, scalable;

	private ImageType( BigDecimal id,
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
	public BigDecimal getId() {
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
}
