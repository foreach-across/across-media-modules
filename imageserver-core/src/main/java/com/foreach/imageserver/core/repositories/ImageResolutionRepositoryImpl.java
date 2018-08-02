package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.ImageResolution;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@RequiredArgsConstructor
public class ImageResolutionRepositoryImpl implements ImageResolutionRepositoryCustom
{
	private final ImageResolutionRepository imageResolutionRepository;

	@Transactional
	public void updateContextsForResolution( long resolutionId, Collection<ImageContext> contexts ) {
		ImageResolution imageResolution = imageResolutionRepository.findOne( resolutionId );
		imageResolution.setContexts( contexts );
		imageResolutionRepository.save( imageResolution );
	}

}
