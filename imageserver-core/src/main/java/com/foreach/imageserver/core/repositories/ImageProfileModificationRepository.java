package com.foreach.imageserver.core.repositories;

import com.foreach.across.modules.hibernate.jpa.repositories.IdBasedEntityJpaRepository;
import com.foreach.imageserver.core.business.ImageProfileModification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImageProfileModificationRepository extends IdBasedEntityJpaRepository<ImageProfileModification>
{
	@Query("select i from ImageProfileModification i where i.imageProfileId = :profileId and i.imageContextId = :contextId and i.imageResolutionId = :resolutionId")
	ImageProfileModification getModification( @Param("profileId") long profileId,
	                                          @Param("contextId") long contextId,
	                                          @Param("resolutionId") long resolutionId );
}
