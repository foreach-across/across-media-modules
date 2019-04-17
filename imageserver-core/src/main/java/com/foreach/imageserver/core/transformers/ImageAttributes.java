package com.foreach.imageserver.core.transformers;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import lombok.Data;

@Data
public class ImageAttributes
{
	private final ImageType type;
	private final Dimensions dimensions;
	private final int sceneCount;
}

