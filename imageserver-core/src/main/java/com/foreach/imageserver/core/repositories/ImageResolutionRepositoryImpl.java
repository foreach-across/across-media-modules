package com.foreach.imageserver.core.repositories;

import com.foreach.across.modules.hibernate.services.HibernateSessionHolder;
import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.ImageResolution;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@RequiredArgsConstructor
public class ImageResolutionRepositoryImpl implements ImageResolutionRepositoryCustom
{
	private final HibernateSessionHolder hibernateSessionHolder;

	@Transactional
	public void updateContextsForResolution( long resolutionId, Collection<ImageContext> contexts ) {
		Session session = hibernateSessionHolder.getCurrentSession();
		ImageResolution imageResolution = session.get( ImageResolution.class, resolutionId );
		imageResolution.setContexts( contexts );
		session.update( imageResolution );
	}

}
