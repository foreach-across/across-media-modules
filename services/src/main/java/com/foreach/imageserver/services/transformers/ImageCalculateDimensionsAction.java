package com.foreach.imageserver.services.transformers;

import com.foreach.imageserver.business.Dimensions;
import com.foreach.imageserver.business.ImageFile;

public class ImageCalculateDimensionsAction extends ImageTransformerAction<Dimensions>
{
	private final ImageFile imageFile;

	public ImageCalculateDimensionsAction( ImageFile imageFile ) {
		this.imageFile = imageFile;
	}

	public ImageFile getImageFile() {
		return imageFile;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		ImageCalculateDimensionsAction that = (ImageCalculateDimensionsAction) o;

		if ( !imageFile.equals( that.imageFile ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return imageFile.hashCode();
	}
}
