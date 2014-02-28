package com.foreach.imageserver.services.transformers;

import com.foreach.imageserver.core.business.ImageFile;

public abstract class ImageTransformerAction<T>
{
	private final ImageFile imageFile;

	protected ImageTransformerAction( ImageFile imageFile ) {
		this.imageFile = imageFile;
	}

	public ImageFile getImageFile() {
		return imageFile;
	}

	private T result;

	public T getResult() {
		return result;
	}

	public void setResult( T result ) {
		this.result = result;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		ImageTransformerAction that = (ImageTransformerAction) o;

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
