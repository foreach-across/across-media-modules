package com.foreach.imageserver.dto;

public enum ImageTypeDto
{
	JPEG( "jpeg" ),
	PNG( "png" ),
	GIF( "gif" ),
	SVG( "svg" ),
	EPS( "eps" ),
	PDF( "pdf" ),
	TIFF( "tif" );

	private String extension;

	ImageTypeDto( String extension ) {
		this.extension = extension;
	}

	public String getExtension() {
		return extension;
	}
}
