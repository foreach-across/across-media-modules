package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.business.ImageModificationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImageModificationRepository extends JpaRepository<ImageModification, ImageModificationId>
{
	@Query("select i from ImageModification i where i.id.imageId = :imageId and i.id.contextId = :contextId and i.id.resolutionId = :imageResolutionId")
	ImageModification getById( @Param("imageId") long imageId,
	                           @Param("contextId") long contextId,
	                           @Param("imageResolutionId") long imageResolutionId );

	@Query("select i from ImageModification i where i.id.imageId = :imageId and i.id.contextId = :contextId")
	List<ImageModification> getModifications( @Param("imageId") long imageId, @Param("contextId") long contextId );

	@Query("select i from ImageModification i where i.id.imageId = :imageId")
	List<ImageModification> getAllModifications( @Param("imageId") long imageId );

	@Query("select count(i)>0 from ImageModification i where i.id.imageId = :imageId")
	boolean hasModification( @Param("imageId") long imageId );
}
