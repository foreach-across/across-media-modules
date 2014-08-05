package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.business.ImageModification;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageModificationDao
{
	ImageModification getById( @Param("imageId") long imageId,
	                           @Param("contextId") long contextId,
	                           @Param("imageResolutionId") long imageResolutionId );

	List<ImageModification> getModifications( @Param("imageId") long imageId, @Param("contextId") long contextId );

	List<ImageModification> getAllModifications( @Param("imageId") long imageId );

	void insert( ImageModification imageModification );

	void update( ImageModification imageModification );

	boolean hasModification( long imageId );
}
