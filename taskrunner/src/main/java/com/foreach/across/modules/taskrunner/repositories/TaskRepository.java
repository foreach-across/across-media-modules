package com.foreach.across.modules.taskrunner.repositories;

import com.foreach.across.modules.hibernate.repositories.BasicRepository;
import com.foreach.across.modules.taskrunner.business.PersistedTask;

import java.util.Collection;

public interface TaskRepository extends BasicRepository<PersistedTask>
{
	Collection<PersistedTask> getAllByHash( String hash );

	PersistedTask getByUuid( String uuid );
}
