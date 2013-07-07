package com.foreach.imageserver.business;

import com.foreach.spring.enums.IdLookup;
import org.apache.commons.lang3.StringUtils;

public enum ImageType implements IdLookup<String>
{
	JPEG( "image/jpeg", "jpeg" ),
	PNG( "image/png", "png" ),
	GIF( "image/gif", "gif" );

	private String contentType, extension;

	private ImageType( String contentType, String extension ) {
		this.contentType = contentType;
		this.extension = extension;
	}

	public String getContentType() {
		return contentType;
	}

	public String getExtension() {
		return extension;
	}

	@Override
	public String getId() {
		return getExtension();
	}

	public static ImageType getForContentType( String contentType ) {
		for ( ImageType imageType : values() ) {
			if ( StringUtils.equalsIgnoreCase( imageType.getContentType(), contentType ) ) {
				return imageType;
			}
		}
		return null;
	}
}
