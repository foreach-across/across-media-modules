package com.foreach.imageserver.dao;

import com.foreach.imageserver.business.image.Crop;
import com.foreach.imageserver.dao.selectors.CropSelector;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropDao
{
    Crop getCropById( long id );

    List<Crop> getCrops( CropSelector selector );

    void insertCrop( Crop crop );

    void updateCrop( Crop crop );

    void deleteCrop( long id );

	void deleteCropsForImage( long imageId );

    Integer getMaxVersion( long imageId );
}
