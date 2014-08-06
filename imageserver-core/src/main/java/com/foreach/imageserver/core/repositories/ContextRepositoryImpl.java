package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.business.Context;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

public class ContextRepositoryImpl extends BasicRepositoryImpl<Context> implements ContextRepository
{
	@Transactional(readOnly = true)
	@SuppressWarnings( "unchecked" )
	@Override
	public Context getByCode( String code ) {
		Criteria criteria = session().createCriteria( Context.class );
		criteria.add( Restrictions.eq( "code", code ) );

		return (Context) criteria.uniqueResult();
	}
}
