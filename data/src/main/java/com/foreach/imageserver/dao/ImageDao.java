package com.foreach.imageserver.dao;

import com.foreach.imageserver.business.Image;
import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.dao.selectors.ImageSelector;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageDao
{
	Image getImageByKey( @Param("key") String key, @Param("applicationId") int applicationId );

	void insertImage( Image image );

	void updateImage( Image image );

	void deleteImage( long imageId );

	ServableImageData getImageById( long id );

	ServableImageData getImageByPath( ImageSelector selector );

	int getImageCount( ImageSelector selector );

	List<ServableImageData> getAllImages();

	void insertImage( ServableImageData image );

	void updateImage( ServableImageData image );

	List<ServableImageData> getImages( ImageSelector selector );
}
