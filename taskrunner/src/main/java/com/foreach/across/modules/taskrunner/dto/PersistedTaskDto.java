package com.foreach.across.modules.taskrunner.dto;

import com.foreach.across.modules.hibernate.dto.IdBasedEntityDto;
import com.foreach.across.modules.taskrunner.business.PersistedTask;
import com.foreach.across.modules.taskrunner.business.TaskStatus;

import java.util.Date;

public class PersistedTaskDto extends IdBasedEntityDto<PersistedTask>
{
	private String uuid;

	private String requestHashCode;

	private String createdBy;

	private String parameters;

	private String result;

	private TaskStatus status;

	private boolean saved;

	private Date created;

	private Date updated;

	private Date expiryDate;

	public String getUuid() {
		return uuid;
	}

	public void setUuid( String uuid ) {
		this.uuid = uuid;
	}

	public String getRequestHashCode() {
		return requestHashCode;
	}

	public void setRequestHashCode( String requestHashCode ) {
		this.requestHashCode = requestHashCode;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy( String createdBy ) {
		this.createdBy = createdBy;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters( String parameters ) {
		this.parameters = parameters;
	}

	public String getResult() {
		return result;
	}

	public void setResult( String result ) {
		this.result = result;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus( TaskStatus status ) {
		this.status = status;
	}

	public boolean isSaved() {
		return saved;
	}

	public void setSaved( boolean saved ) {
		this.saved = saved;
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

	public static PersistedTaskDto fromReportTask( PersistedTask reportTask ) {
		PersistedTaskDto dto = new PersistedTaskDto();
		dto.copyFrom( reportTask );
		return dto;
	}
}
