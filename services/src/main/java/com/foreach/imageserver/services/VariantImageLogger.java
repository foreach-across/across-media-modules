package com.foreach.imageserver.services;

import com.foreach.imageserver.business.image.VariantImage;

public interface VariantImageLogger
{
	void logVariantImage( VariantImage variantImage );

	void flushLog();
}
