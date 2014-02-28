package com.foreach.imageserver.core.business;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class Application
{
	private int id;
	private String name;

	private boolean active;
	private String code;

	private Date dateCreated, dateUpdated;

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

	public String getCode() {
		return code;
	}

	public void setCode( String code ) {
		this.code = code;
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

	public boolean canBeManaged( String code ) {
		return isActive() && getCode() != null && StringUtils.equals( getCode(), code );
	}
}
