package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.business.ImageModification;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageModificationDao {
    ImageModification getById(@Param("imageId") int imageId, @Param("contextId") int contextId, @Param("imageResolutionId") int imageResolutionId);

    List<ImageModification> getModifications(@Param("imageId") int imageId, @Param("contextId") int contextId);

    void insert(ImageModification imageModification);

    boolean hasModification(int imageId);
}
