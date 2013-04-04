package com.foreach.imageserver.example.models;

public class FormatModel
{
	private int applicationId;
	private int groupId;
	private String name;
	private int width;
	private int height;
	private int ratioWidth;
	private int ratioHeight;

	public final int getApplicationId()
	{
		return applicationId;
	}

	public final void setApplicationId( int applicationId )
	{
		this.applicationId = applicationId;
	}

	public final int getGroupId()
	{
		return groupId;
	}

	public final void setGroupId( int groupId )
	{
		this.groupId = groupId;
	}

	public final String getName()
	{
		return name;
	}

	public final void setName( String name )
	{
		this.name = name;
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

	public final int getRatioWidth()
	{
		return ratioWidth;
	}

	public final void setRatioWidth( int ratioWidth )
	{
		this.ratioWidth = ratioWidth;
	}

	public final int getRatioHeight()
	{
		return ratioHeight;
	}

	public final void setRatioHeight( int ratioHeight )
	{
		this.ratioHeight = ratioHeight;
	}
}
