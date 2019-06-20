package com.foreach.imageserver.core.transformers;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class ImageAttributes
{
	private final ImageType type;
	private final Dimensions dimensions;
	private final int sceneCount;

	public static ImageAttributes from( @NonNull Image image ) {
		return new ImageAttributes( image.getImageType(), image.getDimensions(), image.getSceneCount() );
	}
}

