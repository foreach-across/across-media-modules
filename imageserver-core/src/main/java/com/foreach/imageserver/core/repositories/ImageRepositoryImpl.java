package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.business.Image;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ImageRepositoryImpl extends BasicRepositoryImpl<Image> implements ImageRepository
{
	@Transactional(readOnly = true)
	@SuppressWarnings( "unchecked" )
	@Override
	public Image getByExternalId( String externalId ) {
		Criteria criteria = session().createCriteria( Image.class );
		criteria.add( Restrictions.eq( "externalId", externalId ) );

		return (Image) criteria.uniqueResult();
	}
}
