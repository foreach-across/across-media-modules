package com.foreach.imageserver.services;

import com.foreach.imageserver.business.image.ServableImageData;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStoreService
{
	void saveImage( ServableImageData image, MultipartFile imageData );

	void replaceImage( ServableImageData image, MultipartFile imageData, String oldExtension );

	void deleteImage( ServableImageData image );
}
