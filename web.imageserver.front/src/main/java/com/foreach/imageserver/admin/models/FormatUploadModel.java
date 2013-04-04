package com.foreach.imageserver.admin.models;

public class FormatUploadModel
{
	private String userKey;
	private String name;
	private boolean absolute;
	private int width;
	private int height;

	public final String getUserKey()
	{
		return userKey;
	}

	public final void setUserKey( String userKey )
	{
		this.userKey = userKey;
	}

	public final String getName()
	{
		return name;
	}

	public final void setName( String name )
	{
		this.name = name;
	}

	public final boolean isAbsolute()
	{
		return absolute;
	}

	public final void setAbsolute( boolean absolute )
	{
		this.absolute = absolute;
	}

	public final int getWidth()
	{
		return width;
	}

	public final void setWidth( int width )
	{
		this.width = width;
	}

	public final int getHeight()
	{
		return height;
	}

	public final void setHeight( int height )
	{
		this.height = height;
	}
}
