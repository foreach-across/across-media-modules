package com.foreach.imageserver.dto;

public enum ImageTypeDto
{
	JPEG( "jpeg" ),
	PNG( "png" ),
	GIF( "gif" ),
	SVG( "svg" ),
	EPS( "eps" ),
	PDF( "pdf" ),
	TIFF( "tif" ),
	BMP( "bmp" );

	private String extension;

	ImageTypeDto( String extension ) {
		this.extension = extension;
	}

	public String getExtension() {
		return extension;
	}

	public static ImageTypeDto forExtension( String extension ) {
		for ( ImageTypeDto value : values() ) {
			if ( value.getExtension().equals( extension ) ) {
				return value;
			}
		}
		return null;
	}
}
