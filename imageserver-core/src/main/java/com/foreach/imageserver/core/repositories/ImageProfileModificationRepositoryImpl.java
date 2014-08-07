package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.business.ImageProfileModification;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ImageProfileModificationRepositoryImpl extends BasicRepositoryImpl<ImageProfileModification> implements ImageProfileModificationRepository
{
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	@Override
	public ImageProfileModification getModification( long imageProfileId, long contextId, long imageResolutionId ) {
		Criteria criteria = session().createCriteria( ImageProfileModification.class );
		criteria.add( Restrictions.eq( "imageProfileId", imageProfileId ) );
		criteria.add( Restrictions.eq( "imageContextId", contextId ) );
		criteria.add( Restrictions.eq( "imageResolutionId", imageResolutionId ) );

		return (ImageProfileModification) criteria.uniqueResult();
	}
}
