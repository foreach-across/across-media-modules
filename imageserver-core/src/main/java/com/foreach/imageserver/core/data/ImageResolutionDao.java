package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.business.ImageResolution;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageResolutionDao {
    ImageResolution getById(int resolutionId);

    List<ImageResolution> getForContext(@Param("contextId") int contextId);

    void insertResolution(ImageResolution resolution);

    void updateResolution(ImageResolution resolution);

    List<ImageResolution> getAllResolutions();

    ImageResolution getByDimensions(@Param("width") int width, @Param("height") int height);
}
