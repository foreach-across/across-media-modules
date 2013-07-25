package com.foreach.imageserver.services.repositories;

import com.foreach.imageserver.business.ImageType;
import com.foreach.imageserver.business.Dimensions;

import java.io.InputStream;

public final class RepositoryLookupResult
{
	private ImageType imageType;
	@Deprecated
	private Dimensions dimensions;
	private RepositoryLookupStatus status;
	private InputStream content;

	public RepositoryLookupStatus getStatus() {
		return status;
	}

	public void setStatus( RepositoryLookupStatus status ) {
		this.status = status;
	}

	@Deprecated
	public Dimensions getDimensions() {
		return dimensions;
	}

	@Deprecated
	public void setDimensions( Dimensions dimensions ) {
		this.dimensions = dimensions;
	}

	public ImageType getImageType() {
		return imageType;
	}

	public void setImageType( ImageType imageType ) {
		this.imageType = imageType;
	}

	public InputStream getContent() {
		return content;
	}

	public void setContent( InputStream content ) {
		this.content = content;
	}
}
