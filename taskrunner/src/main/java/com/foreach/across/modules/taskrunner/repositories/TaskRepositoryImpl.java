package com.foreach.across.modules.taskrunner.repositories;

import com.foreach.across.modules.hibernate.repositories.BasicRepositoryImpl;
import com.foreach.across.modules.taskrunner.business.ReportTask;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Repository
public class TaskRepositoryImpl extends BasicRepositoryImpl<ReportTask>
		implements TaskRepository
{
	@Override
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public Collection<ReportTask> getAllByHash( String hash ) {
		return (Collection<ReportTask>) distinct()
				.add( Restrictions.eq( "requestHashCode", hash ) )
				.list();
	}

	@Override
	public ReportTask getByUuid( String uuid ) {
		return (ReportTask) distinct().add( Restrictions.eq( "uuid", uuid ) ).uniqueResult();
	}
}
