package com.foreach.imageserver.core.services;

import com.foreach.across.core.registry.RefreshableRegistry;
import org.springframework.stereotype.Component;

@Component
public class ImageRepositoryRegistry extends RefreshableRegistry<ImageRepository>
{
	public ImageRepositoryRegistry() {
		super( ImageRepository.class, true );
	}
}
