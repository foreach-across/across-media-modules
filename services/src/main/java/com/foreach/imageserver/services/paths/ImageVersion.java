package com.foreach.imageserver.services.paths;


public enum ImageVersion
{
	ORIGINAL( "originals" ),
	VARIANT( "variants" );

	private final String path;

	ImageVersion( String path )
	{
		this.path = path;
	}

	public final String getPath()
	{
		return path;
	}
}
