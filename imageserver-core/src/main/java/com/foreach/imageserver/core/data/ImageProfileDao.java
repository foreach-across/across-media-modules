package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.business.ImageProfileModification;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageProfileDao {
    ImageProfileModification getModification(@Param("profileId") int profileId, @Param("contextId") int contextId, @Param("resolutionId") int resolutionId);
}