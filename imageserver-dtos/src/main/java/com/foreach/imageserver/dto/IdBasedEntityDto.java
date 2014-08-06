package com.foreach.imageserver.dto;

import java.util.Objects;

public class IdBasedEntityDto
{
	private long id;
	private Boolean newEntity;

	public long getId() {
		return id;
	}

	public void setId( long id ) {
		this.id = id;
	}

	public boolean isNewEntity() {
		return newEntity != null ? newEntity : getId() == 0;
	}

	public void setNewEntity( boolean newEntity ) {
		this.newEntity = newEntity;
	}

	@SuppressWarnings( "all" )
	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( !( o instanceof IdBasedEntityDto ) ) {
			return false;
		}

		IdBasedEntityDto that = (IdBasedEntityDto) o;

		return Objects.equals( id, that.id );
	}

	@SuppressWarnings( "all" )
	@Override
	public int hashCode() {
		return Objects.hash( id );
	}
}
