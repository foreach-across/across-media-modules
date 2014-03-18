package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.business.Image;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageDao {
    Image getById(@Param("imageId") int imageId);

    void insert(Image image);
}
