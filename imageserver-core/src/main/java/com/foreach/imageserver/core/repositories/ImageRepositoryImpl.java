package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.business.Image;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

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

	@Override
	@Transactional
	public void create( Image object ) {
		setPath( object );
		super.create( object );
	}

	@Override
	@Transactional
	public void update( Image object ) {
		setPath( object );
		super.update( object );
	}

	private void setPath( Image object ) {
		Date dateCreated = object.getDateCreated();
		if( dateCreated == null || ( StringUtils.isBlank( object.getOriginalPath() ) && StringUtils.isBlank( object.getVariantPath() ) ) ) {
			String path = FastDateFormat.getInstance( "yyyy/MM/dd" ).format( dateCreated );
			object.setVariantPath( path );
			object.setOriginalPath( path );
		}
	}
}
