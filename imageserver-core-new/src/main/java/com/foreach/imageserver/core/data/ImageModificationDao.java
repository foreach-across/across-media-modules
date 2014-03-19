package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.business.ImageModification;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageModificationDao {
    ImageModification getById(@Param("imageId") int imageId, @Param("contextId") int contextId, @Param("imageResolutionId") int imageResolutionId);

    void insert(ImageModification imageModification);

    boolean hasModification(int imageId);
}
