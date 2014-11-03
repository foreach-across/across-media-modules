package com.foreach.across.modules.taskrunner.repositories;

import com.foreach.across.modules.hibernate.repositories.BasicRepositoryImpl;
import com.foreach.across.modules.taskrunner.business.PersistedTask;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Repository
public class TaskRepositoryImpl extends BasicRepositoryImpl<PersistedTask>
		implements TaskRepository
{
	@Override
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public Collection<PersistedTask> getAllByHash( String hash ) {
		return (Collection<PersistedTask>) distinct()
				.add( Restrictions.eq( "requestHashCode", hash ) )
				.list();
	}

	@Override
	public PersistedTask getByUuid( String uuid ) {
		return (PersistedTask) distinct().add( Restrictions.eq( "uuid", uuid ) ).uniqueResult();
	}
}
