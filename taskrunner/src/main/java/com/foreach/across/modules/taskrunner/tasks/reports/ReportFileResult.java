package com.foreach.across.modules.taskrunner.tasks.reports;

import java.util.Objects;

public abstract class ReportFileResult
{
	private String name;
	private String path;
	private ReportFormat type;

	protected ReportFileResult( String name, String path, ReportFormat type ) {
		this.name = name;
		this.path = path;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath( String path ) {
		this.path = path;
	}

	public ReportFormat getType() {
		return type;
	}

	public void setType( ReportFormat type ) {
		this.type = type;
	}

	@Override
	public boolean equals( Object obj ) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}

		ReportFileResult that = (ReportFileResult) obj;
		return Objects.equals( this.getType(), that.getType() ) && Objects.equals( getName(), that.getName() ) && Objects.equals( getPath(), that.getPath() );
	}

	@Override
	public int hashCode() {
		return Objects.hash( getType(), getName(), getPath() );
	}
}
