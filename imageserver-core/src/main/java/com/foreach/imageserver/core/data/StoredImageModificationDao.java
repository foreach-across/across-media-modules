package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.business.StoredImageModification;
import com.foreach.imageserver.core.business.ImageVariant;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface StoredImageModificationDao {

    List<StoredImageModification> getModificationsForImage(int imageId);

    void insertModification(StoredImageModification variant);

    void updateModification(StoredImageModification variant);

    StoredImageModification getModification(@Param("imageId") int imageId, @Param("variant") ImageVariant variant);

    void deleteModification(@Param("imageId") int imageId, @Param("variant") ImageVariant variant);
}
