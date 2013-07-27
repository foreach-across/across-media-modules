package com.foreach.imageserver.business;

import com.foreach.spring.enums.IdLookup;
import org.apache.commons.lang3.StringUtils;

public enum ImageType implements IdLookup<String>
{
	JPEG( "image/jpeg", "jpeg" ),
	PNG( "image/png", "png" ),
	GIF( "image/gif", "gif" ),
	SVG( "image/svg+xml", "svg" ),
	EPS( "application/postscript", "eps", "image/eps", "image/x-eps", "application/eps", "application/x-eps" ),
	PDF( "application/pdf", "pdf", "application/x-pdf" ),
	TIFF( "image/tiff", "tif" );

	private String contentType, extension;
	private String[] alternativeContentTypes;

	private ImageType( String contentType, String extension, String... alternativeContentTypes )
	{
		this.contentType = contentType;
		this.extension = extension;
		this.alternativeContentTypes = alternativeContentTypes;
	}

	public String getContentType()
	{
		return contentType;
	}

	public String getExtension()
	{
		return extension;
	}

	@Override
	public String getId()
	{
		return getExtension();
	}

	public static ImageType getForContentType( String contentType )
	{
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
}
