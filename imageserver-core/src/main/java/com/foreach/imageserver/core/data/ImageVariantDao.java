package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.business.StoredImageVariant;
import com.foreach.imageserver.core.business.ImageModifier;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ImageVariantDao {

    Collection<StoredImageVariant> getVariantsForImage(int imageId);

    void insertVariant(StoredImageVariant variant);

    void updateVariant(StoredImageVariant variant);

    StoredImageVariant getVariant(@Param("imageId") int imageId, @Param("modifier") ImageModifier modifier);

    void deleteVariant(@Param("imageId") int imageId, @Param("modifier") ImageModifier modifier);
}
