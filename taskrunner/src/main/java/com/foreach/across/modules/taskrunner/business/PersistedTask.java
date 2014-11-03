package com.foreach.across.modules.taskrunner.business;

import com.foreach.across.modules.hibernate.business.IdBasedEntity;
import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import com.foreach.across.modules.taskrunner.config.TaskRunnerSchemaConfiguration;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * Persisted version of a task, containing the serialized parameters and result.
 *
 * @author Arne Vandamme
 */
@Entity
@Table(name = TaskRunnerSchemaConfiguration.TABLE_TASK)
public class PersistedTask implements IdBasedEntity
{
	@Id
	@GeneratedValue(generator = "seq_rm_report_task_id")
	@GenericGenerator(
			name = "seq_rm_report_task_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_rm_report_task_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "5")
			}
	)
	private long id;

	@Column(name = "uuid")
	private String uuid;

	@Column(name = "hash_code")
	private String requestHashCode;

	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "parameters")
	private String parameters;

	@Column(name = "result")
	private String result;

	@Column(name = "status")
	private TaskStatus status;

	@Column(name = "saved")
	private boolean saved;

	@Column(name = "created")
	private Date created;

	@Column(name = "updated")
	private Date updated;

	@Column(name = "expiry_date")
	private Date expiryDate;

	/**
	 * @return Unique id of this report task.
	 */
	@Override
	public long getId() {
		return id;
	}

	public void setId( long id ) {
		this.id = id;
	}

	/**
	 * @return The UUID of the task.
	 */
	public String getUuid() {
		return uuid;
	}

	public void setUuid( String uuid ) {
		this.uuid = uuid;
	}

	/**
	 * @return Hash Code of all the parameters
	 */
	public String getRequestHashCode() {
		return requestHashCode;
	}

	public void setRequestHashCode( String hashCode ) {
		this.requestHashCode = hashCode;
	}

	/**
	 * @return The principal that created the original task.
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy( String createdBy ) {
		this.createdBy = createdBy;
	}

	/**
	 * @return The original report parameters used for generating the report.
	 */
	public String getParameters() {
		return parameters;
	}

	public void setParameters( String parameters ) {
		this.parameters = parameters;
	}

	/**
	 * @return The result attached to this task.  Can be null.
	 */
	public String getResult() {
		return result;
	}

	public void setResult( String result ) {
		this.result = result;
	}

	/**
	 * @return Current status of the report task.
	 */
	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus( TaskStatus status ) {
		this.status = status;
	}

	/**
	 * @return Timestamp when this task was originally created.
	 */
	public Date getCreated() {
		return created;
	}

	public void setCreated( Date created ) {
		this.created = created;
	}

	/**
	 * @return Timestamp of the last update (status change).
	 */
	public Date getUpdated() {
		return updated;
	}

	public void setUpdated( Date updated ) {
		this.updated = updated;
	}

	/**
	 * @return True if this report is
	 */
	public boolean isSaved() {
		return saved;
	}

	public void setSaved( boolean saved ) {
		this.saved = saved;
	}

	/**
	 * @return Date at which time the report expires.  Null if it never expires.
	 */
	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate( Date expiryDate ) {
		this.expiryDate = expiryDate;
	}

	@Override
	public boolean equals( Object obj ) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}

		PersistedTask that = (PersistedTask) obj;
		return Objects.equals( this.getUuid(), that.getUuid() );
	}

	@Override
	public int hashCode() {
		return Objects.hashCode( getUuid() );
	}
}
