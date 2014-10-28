package com.foreach.across.modules.taskrunner.repositories;

import com.foreach.across.modules.hibernate.repositories.BasicRepository;
import com.foreach.across.modules.taskrunner.business.ReportTask;

import java.util.Collection;

public interface TaskRepository extends BasicRepository<ReportTask>
{
	Collection<ReportTask> getAllByHash( String hash );

	ReportTask getByUuid( String uuid );
}
