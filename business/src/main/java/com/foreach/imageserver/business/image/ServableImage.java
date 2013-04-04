package com.foreach.imageserver.business.image;

import com.foreach.imageserver.business.geometry.Size;

import java.util.Date;

public abstract class ServableImage
{
	private long id;
	private int applicationId;
    private int groupId;
    private int width;
    private int height;
	private long fileSize;
	private String path;
	private String originalFileName;
	private String extension;
	private Date dateCreated;
	private boolean deleted;

    public final long getId()
	{
		return id;
	}

	public final void setId( long id )
	{
		this.id = id;
	}

	public final int getApplicationId()
	{
		return applicationId;
	}

	public final int getGroupId()
	{
		return groupId;
	}

	public final int getWidth()
	{
		return width;
	}

	public final int getHeight()
	{
		return height;
	}

	public final Size getSize()
	{
		return new Size( getWidth(), getHeight() );
	}

	public final long getFileSize()
	{
		return fileSize;
	}

	public final String getPath()
	{
		return path;
	}

	public final String getOriginalFileName()
	{
		return originalFileName;
	}

	public final String getExtension()
	{
		return extension;
	}

	public final void setApplicationId( int applicationId )
	{
		this.applicationId = applicationId;
	}

	public final void setGroupId( int groupId )
	{
		this.groupId = groupId;
	}

	public final void setWidth( int width )
	{
		this.width = width;
	}

	public final void setHeight( int height )
	{
		this.height = height;
	}

	public final void setFileSize( long fileSize )
	{
		this.fileSize = fileSize;
	}

	public final void setPath( String path )
	{
		this.path = path;
	}

	public final void setOriginalFileName( String originalFileName )
	{
		this.originalFileName = originalFileName;
	}

	public final void setExtension( String extension )
	{
		this.extension = extension;
	}

	public final Date getDateCreated()
	{
		return dateCreated;
	}

	public final void setDateCreated( Date dateCreated )
	{
		this.dateCreated = dateCreated;
	}

	public final boolean isDeleted()
	{
		return deleted;
	}

	public final void setDeleted( boolean deleted )
	{
		this.deleted = deleted;
	}
}
