package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.business.ImageModification;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class ImageModificationRepositoryImpl extends BasicRepositoryImpl<ImageModification> implements ImageModificationRepository
{
	@Transactional(readOnly = true)
	@SuppressWarnings( "unchecked" )
	@Override
	public ImageModification getById( long imageId, long contextId, long imageResolutionId ) {
		Criteria criteria = session().createCriteria( ImageModification.class );
		criteria.add( Restrictions.eq( "imageId", imageId ) );
		criteria.add( Restrictions.eq( "contextId", contextId ) );
		criteria.add( Restrictions.eq( "resolutionId", imageResolutionId ) );

		return (ImageModification) criteria.uniqueResult();
	}

	@Override
	@Transactional(readOnly = true)
	@SuppressWarnings( "unchecked" )
	public List<ImageModification> getModifications( long imageId, long contextId ) {
		Criteria criteria = session().createCriteria( ImageModification.class );
		criteria.add( Restrictions.eq( "imageId", imageId ) );
		criteria.add( Restrictions.eq( "contextId", contextId ) );

		criteria.addOrder( Order.asc( "resolutionId" ) );

		return (List) criteria.list();
	}

	@Override
	@Transactional(readOnly = true)
	@SuppressWarnings( "unchecked" )
	public List<ImageModification> getAllModifications( long imageId ) {
		Criteria criteria = session().createCriteria( ImageModification.class );
		criteria.add( Restrictions.eq( "imageId", imageId ) );

		return (List) criteria.list();
	}

	@Override
	@Transactional(readOnly = true)
	@SuppressWarnings( "unchecked" )
	public boolean hasModification( long imageId ) {
		Criteria criteria = session().createCriteria( ImageModification.class );
		criteria.add( Restrictions.eq( "imageId", imageId ) );
		criteria.setProjection( Projections.rowCount() );
		Long number = (Long) criteria.uniqueResult();
		return number != 0;
	}
}
