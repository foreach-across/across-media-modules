package com.foreach.imageserver.business;

import org.apache.commons.lang3.ObjectUtils;

import java.util.UUID;

public final class Application
{
	private int id;
	private String name;

	private boolean active;
	private UUID code;

	@Deprecated
	private String callbackUrl;

	public int getId() {
		return id;
	}

	public void setId( int id ) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl( String callbackUrl ) {
		this.callbackUrl = callbackUrl;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive( boolean active ) {
		this.active = active;
	}

	public UUID getCode() {
		return code;
	}

	public void setCode( UUID code ) {
		this.code = code;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		Application that = (Application) o;

		if ( id != that.id ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return id;
	}

	public boolean canBeManaged( UUID code ) {
		return isActive() && getCode() != null && ObjectUtils.equals( getCode(), code );
	}
}
