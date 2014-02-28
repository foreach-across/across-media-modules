package com.foreach.imageserver.services.transformers;

import com.foreach.imageserver.business.Dimensions;
import com.foreach.imageserver.business.ImageFile;

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
