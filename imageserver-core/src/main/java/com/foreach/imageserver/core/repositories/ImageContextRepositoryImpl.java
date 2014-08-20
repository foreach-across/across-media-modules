package com.foreach.imageserver.core.repositories;

import com.foreach.across.modules.hibernate.repositories.BasicRepositoryImpl;
import com.foreach.imageserver.core.business.ImageContext;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

public class ImageContextRepositoryImpl extends BasicRepositoryImpl<ImageContext> implements ImageContextRepository
{
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	@Override
	public ImageContext getByCode( String code ) {
		Criteria criteria = session().createCriteria( ImageContext.class );
		criteria.add( Restrictions.eq( "code", code ) );

		return (ImageContext) criteria.uniqueResult();
	}
}
