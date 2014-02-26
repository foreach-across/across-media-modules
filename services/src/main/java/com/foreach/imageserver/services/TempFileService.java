package com.foreach.imageserver.services;

import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.ImageType;

import java.io.File;
import java.io.InputStream;

public interface TempFileService
{
	ImageFile createImageFile( ImageType imageType, InputStream stream );

	boolean isTempFile( ImageFile imageFile );

	ImageFile move( ImageFile imageFile, File physicalDestination );
}
