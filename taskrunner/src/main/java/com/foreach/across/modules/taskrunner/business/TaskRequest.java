package com.foreach.across.modules.taskrunner.business;

import java.util.Date;

/**
 * Represents a report request instance.
 *
 * The type T must represent the different report parameters that can be set on the report.  These must be
 * serializable and deserializable to and from valid xml/json (xstream xml?).
 *
 * @author Arne Vandamme
 */
public class TaskRequest<T>
{
	private T parameters;

	private boolean saveResult;
	private boolean forceExecution;

	private Date expiryDate, oldestResultDate;

	private String createdBy;


	public T getParameters() {
		return parameters;
	}

	public void setParameters( T parameters ) {
		this.parameters = parameters;
	}

	/**
	 * @return The principal that requests this task.
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy( String createdBy ) {
		this.createdBy = createdBy;
	}

	/**
	 * @return True if the result can be stored in database (task can be stored).
	 */
	public boolean isSaveResult() {
		return saveResult;
	}

	public void setSaveResult( boolean saveResult ) {
		this.saveResult = saveResult;
	}

	/**
	 * If true, report will be generated and will ignore currently stored versions, or replace them.
	 *
	 * @return True if the report should be generated.
	 */
	public boolean isForceExecution() {
		return forceExecution;
	}

	public void setForceExecution( boolean forceExecution ) {
		this.forceExecution = forceExecution;
	}

	/**
	 * @return Date when the report should expire if saveResult is true.  Null means report will be kept indefinitely.
	 */
	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate( Date expiryDate ) {
		this.expiryDate = expiryDate;
	}

	/**
	 * @return If a saved task is found, the creation date of that task should not be before this date.  Even if the task
	 * is still busy.
	 */
	public Date getOldestResultDate() {
		return oldestResultDate;
	}

	public void setOldestResultDate( Date oldestResultDate ) {
		this.oldestResultDate = oldestResultDate;
	}
}
