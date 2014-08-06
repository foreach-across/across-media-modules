package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.business.ContextImageResolution;
import com.foreach.imageserver.core.business.ImageResolution;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ImageResolutionRepositoryImpl extends BasicRepositoryImpl<ImageResolution> implements ImageResolutionRepository
{
	@Override
	@SuppressWarnings( "unchecked" )
	@Transactional(readOnly = true)
	public List<ImageResolution> getForContext( long contextId ) {
		Criteria criteria = session().createCriteria( ContextImageResolution.class );
		criteria.add( Restrictions.eq( "contextId", contextId ) );

		List<ContextImageResolution> contextImageResolutions = criteria.list();
		List<ImageResolution> imageResolutions = new ArrayList<>();
		for( ContextImageResolution contextImageResolution : contextImageResolutions ) {
			//TODO: optimize me using JoinType.INNER_JOIN
			ImageResolution imageResolution = getById( contextImageResolution.getImageResolutionId() );
			imageResolutions.add( imageResolution );
		}
		return imageResolutions;
	}

	@Override
	@Transactional( readOnly = true)
	public ImageResolution getByDimensions( int width, int height ) {
		Criteria criteria = session().createCriteria( ImageResolution.class );
		criteria.add( Restrictions.eq( "width", width ) );
		criteria.add( Restrictions.eq( "height", height ) );
		return (ImageResolution) criteria.uniqueResult();
	}
}
