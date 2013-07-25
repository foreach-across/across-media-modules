package com.foreach.imageserver.services.transformers;

import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.ImageModifier;

public class ImageModifyAction extends ImageTransformerAction<ImageFile>
{
	private final ImageFile original;
	private final ImageModifier modifier;

	public ImageModifyAction( ImageFile original, ImageModifier modifier ) {
		this.original = original;
		this.modifier = modifier;
	}

	public ImageFile getOriginal() {
		return original;
	}

	public ImageModifier getModifier() {
		return modifier;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		ImageModifyAction that = (ImageModifyAction) o;

		if ( !modifier.equals( that.modifier ) ) {
			return false;
		}
		if ( !original.equals( that.original ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = original.hashCode();
		result = 31 * result + modifier.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "ImageModifyAction{" +
				"original=" + original +
				", modifier=" + modifier +
				'}';
	}
}
