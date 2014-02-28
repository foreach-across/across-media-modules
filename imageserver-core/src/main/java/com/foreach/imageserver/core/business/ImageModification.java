package com.foreach.imageserver.core.business;

import java.util.Date;

public class ImageModification
{
	private int imageId;
	private ImageModifier modifier = new ImageModifier();
	private Dimensions dimensions = new Dimensions();
	private Date dateCreated;
	private Date dateUpdated;

	public int getImageId() {
		return imageId;
	}

	public void setImageId( int imageId ) {
		this.imageId = imageId;
	}

	public ImageModifier getModifier() {
		return modifier;
	}

	public void setModifier( ImageModifier modifier ) {
		this.modifier = modifier;
	}

	public Dimensions getDimensions() {
		return dimensions;
	}

	public void setDimensions( Dimensions dimensions ) {
		this.dimensions = dimensions;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated( Date dateCreated ) {
		this.dateCreated = dateCreated;
	}

	public Date getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated( Date dateUpdated ) {
		this.dateUpdated = dateUpdated;
	}
}
