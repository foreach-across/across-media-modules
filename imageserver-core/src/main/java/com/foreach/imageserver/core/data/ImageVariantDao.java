package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.business.ImageVariant;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageVariantDao {

    List<ImageVariant> getVariantsForApplication(int applicationId);

    void insertVariant(@Param("applicationId") int applicationId, @Param("variant") ImageVariant variant);

}
