package com.foreach.imageserver.services.transformers;

import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.ImageModifier;

public class ImageModifyAction extends ImageTransformerAction<ImageFile>
{
	private final ImageModifier modifier;

	public ImageModifyAction( ImageFile original, ImageModifier modifier ) {
		super( original );
		this.modifier = modifier;
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
		if ( !super.equals( o ) ) {
			return false;
		}

		ImageModifyAction that = (ImageModifyAction) o;

		if ( modifier != null ? !modifier.equals( that.modifier ) : that.modifier != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + ( modifier != null ? modifier.hashCode() : 0 );
		return result;
	}

	@Override
	public String toString() {
		return "ImageModifyAction{" +
				"original=" + getImageFile() +
				", modifier=" + modifier +
				'}';
	}
}
