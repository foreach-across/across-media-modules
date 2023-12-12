package com.foreach.imageserver.core.hibernate;

import com.foreach.across.modules.hibernate.types.HibernateBitFlag;
import com.foreach.imageserver.core.business.ImageType;

/**
 * @author Arne Vandamme
 */
public class ImageTypeSetUserType extends HibernateBitFlag<ImageType>
{
	public static final String CLASS_NAME = "com.foreach.imageserver.core.hibernate.ImageTypeSetUserType";

	public ImageTypeSetUserType() {
		super( ImageType.class );
	}
}
