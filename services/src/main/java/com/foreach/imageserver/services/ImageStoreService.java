package com.foreach.imageserver.services;

import com.foreach.imageserver.business.Image;
import com.foreach.imageserver.business.image.ServableImageData;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface ImageStoreService
{
	void saveImage( ServableImageData image, MultipartFile imageData );

	void replaceImage( ServableImageData image, MultipartFile imageData, String oldExtension );

	void deleteImage( ServableImageData image );

	String generateRelativeImagePath( Image image );

	long saveImage( Image image, InputStream imageData );

	void deleteVariants( Image image );
}
