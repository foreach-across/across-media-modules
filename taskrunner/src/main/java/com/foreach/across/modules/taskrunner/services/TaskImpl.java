package com.foreach.across.modules.taskrunner.services;

import com.foreach.across.modules.taskrunner.business.MutableTask;
import com.foreach.across.modules.taskrunner.business.TaskStatus;

import java.util.Date;

/**
 * @author Arne Vandamme
 */
public class TaskImpl implements MutableTask
{
	private final String id;
	private String createdBy;
	private Object parameters;
	private Object result;
	private TaskStatus status;
	private Date created;
	private Date updated;
	private Date expiryDate;

	public TaskImpl( String id ) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy( String createdBy ) {
		this.createdBy = createdBy;
	}

	@SuppressWarnings("unchecked")
	public <T> T getParameters() {
		return (T) parameters;
	}

	public void setParameters( Object parameters ) {
		this.parameters = parameters;
	}

	@SuppressWarnings("unchecked")
	public <T> T getResult() {
		return (T) result;
	}

	public void setResult( Object result ) {
		this.result = result;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus( TaskStatus status ) {
		this.status = status;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated( Date created ) {
		this.created = created;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated( Date updated ) {
		this.updated = updated;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate( Date expiryDate ) {
		this.expiryDate = expiryDate;
	}

	@Override
	public void touch() {
		throw new UnsupportedOperationException( "Not yet implemented." );
	}
}
