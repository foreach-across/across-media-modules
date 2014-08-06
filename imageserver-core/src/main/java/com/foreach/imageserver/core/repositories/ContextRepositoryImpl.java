package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.business.Context;
import com.foreach.imageserver.core.business.ContextImageResolution;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

	@Override
	@Transactional(readOnly = true)
	@SuppressWarnings( "unchecked" )
	public Collection<Context> getForResolution( long resolutionId ) {
		Criteria criteria = session().createCriteria( ContextImageResolution.class );
		criteria.add( Restrictions.eq( "imageResolutionId", resolutionId ) );

		List<ContextImageResolution> contextImageResolutions = criteria.list();
		List<Context> contexts = new ArrayList<>();
		for( ContextImageResolution contextImageResolution : contextImageResolutions ) {
			//TODO: optimize me using JoinType.INNER_JOIN
			Context context = getById( contextImageResolution.getContextId() );
			contexts.add( context );
		}
		return contexts;
	}


}
