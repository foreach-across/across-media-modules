package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.business.ContextImageResolution;
import com.foreach.imageserver.core.business.ImageResolution;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Repository
public class ImageResolutionRepositoryImpl extends BasicRepositoryImpl<ImageResolution> implements ImageResolutionRepository
{
	@Override
	@SuppressWarnings( "unchecked" )
	@Transactional(readOnly = true)
	public List<ImageResolution> getForContext( long contextId ) {
		Criteria criteria = session().createCriteria( ContextImageResolution.class );
		//criteria.createAlias(  )
		criteria.add( Restrictions.eq( "contextId", contextId ) );
		Criteria detachedCriteria = criteria.createCriteria( "image_resolution_id", JoinType.INNER_JOIN );
		
		List<ContextImageResolution> contextImageResolutions = criteria.list();
		return Collections.emptyList();
		//return contextImageResolutions;
	}
}
