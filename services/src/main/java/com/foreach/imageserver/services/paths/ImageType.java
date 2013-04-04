package com.foreach.imageserver.services.paths;


public enum ImageType
{
	ORIGINAL( "originals" ),
	VARIANT( "variants" );

	private final String path;

	ImageType( String path )
	{
		this.path = path;
	}

	public final String getPath()
	{
		return path;
	}
}
