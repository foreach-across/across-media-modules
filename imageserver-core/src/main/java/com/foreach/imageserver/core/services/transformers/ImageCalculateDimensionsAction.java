package com.foreach.imageserver.core.services.transformers;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageFile;

public class ImageCalculateDimensionsAction extends ImageTransformerAction<Dimensions>
{
	public ImageCalculateDimensionsAction( ImageFile imageFile ) {
		super( imageFile );
	}

	@Override
	public String toString() {
		return "ImageCalculateDimensionsAction{" +
				"imageFile=" + getImageFile() +
				'}';
	}
}
