package com.foreach.imageserver.core.transformers;

import com.foreach.across.core.registry.RefreshableRegistry;

public class ImageTransformerRegistry extends RefreshableRegistry<ImageTransformer>
{
	public ImageTransformerRegistry() {
		super( ImageTransformer.class, true );
	}
}