package com.foreach.imageserver.core.business;

import com.foreach.imageserver.core.config.ImageSchemaConfiguration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name= ImageSchemaConfiguration.TABLE_CONTEXT_IMAGE_RESOLUTION)
public class ContextImageResolution implements Serializable
{
	@Id
	@Column( name = "context_id" )
	private long contextId;

	@Id
	@Column( name = "image_resolution_id" )
	private long imageResolutionId;

	public long getContextId() {
		return contextId;
	}

	public void setContextId( long contextId ) {
		this.contextId = contextId;
	}

	public long getImageResolutionId() {
		return imageResolutionId;
	}

	public void setImageResolutionId( long imageResolutionId ) {
		this.imageResolutionId = imageResolutionId;
	}
}
