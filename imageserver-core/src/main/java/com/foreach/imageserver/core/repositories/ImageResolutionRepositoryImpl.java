package com.foreach.imageserver.core.repositories;

import com.foreach.across.modules.hibernate.repositories.BasicRepositoryImpl;
import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.ImageResolution;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Repository
public class ImageResolutionRepositoryImpl extends BasicRepositoryImpl<ImageResolution> implements ImageResolutionRepository
{
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List<ImageResolution> getForContext( long contextId ) {
		String hql = "select distinct r " +
				"from ImageResolution r join r.contexts c " +
				"where c.id = :contextId order by r.id";
		Query query = session().createQuery( hql );
		query.setParameter( "contextId", contextId );

		return query.list();
	}

	@Override
	@Transactional(readOnly = true)
	public ImageResolution getByDimensions( int width, int height ) {
		Criteria criteria = session().createCriteria( ImageResolution.class );
		criteria.add( Restrictions.eq( "width", width ) );
		criteria.add( Restrictions.eq( "height", height ) );
		return (ImageResolution) criteria.uniqueResult();
	}

	@Override
	@Transactional
	public void updateContextsForResolution( long resolutionId, Collection<ImageContext> contexts ) {
		ImageResolution imageResolution = getById( resolutionId );
		imageResolution.setContexts( contexts );
		update( imageResolution );
	}
}
