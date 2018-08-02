package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.business.Image;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@RequiredArgsConstructor
public class ImageRepositoryImpl implements ImageRepositoryCustom
{
	private final ImageRepository imageRepository;
	@Override
	@Transactional
	public void create( Image object ) {
		save( object );
	}

	@Override
	@Transactional
	public void update( Image object ) {
		save( object );
	}

	private void save( Image object ) {
		setPath( object );
		imageRepository.save( object );
	}

	private void setPath( Image object ) {
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
