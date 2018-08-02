package com.foreach.imageserver.core.repositories;

import com.foreach.across.modules.hibernate.jpa.repositories.IdBasedEntityJpaRepository;
import com.foreach.imageserver.core.business.Image;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;

public interface ImageRepository extends IdBasedEntityJpaRepository<Image>
{
	Image getByExternalId( String externalId );

	default void update( Image object ) {
		setPath( object );
		save( object );
	}

	default void create( Image object ) {
		setPath( object );
		save( object );
	}

	default void setPath( Image object ) {
		Date dateCreated = object.getDateCreated();
		if ( dateCreated == null || (
				StringUtils.isBlank( object.getOriginalPath() ) && StringUtils.isBlank( object.getVariantPath() )
		) ) {
			String path = FastDateFormat.getInstance( "yyyy/MM/dd/HH" ).format( dateCreated );
			object.setVariantPath( path );
			object.setOriginalPath( path );
		}
	}
}
