package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.business.StoredImageModification;
import com.foreach.imageserver.core.business.ImageVariant;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface StoredImageModificationDao {

    Collection<StoredImageModification> getModificationsForImage(int imageId);

    void insertModification(StoredImageModification variant);

    void updateModification(StoredImageModification variant);

    StoredImageModification getModification(@Param("imageId") int imageId, @Param("variant") ImageVariant variant);

    void deleteModification(@Param("imageId") int imageId, @Param("variant") ImageVariant variant);
}
