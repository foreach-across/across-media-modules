package com.foreach.imageserver.data;

import com.foreach.imageserver.business.Image;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageDao
{
	Image getImageByKey( @Param("key") String key, @Param("applicationId") int applicationId );

	void insertImage( Image image );

	void updateImage( Image image );

	void deleteImage( long imageId );
}
