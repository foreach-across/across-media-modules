package com.foreach.imageserver.services.crop;

import com.foreach.imageserver.business.image.Crop;
import com.foreach.imageserver.business.math.Fraction;
import com.foreach.imageserver.dao.CropDao;
import com.foreach.imageserver.dao.selectors.CropSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CropServiceImpl implements CropService
{
    @Autowired
    private CropDao cropDao;

    public final Crop getCropById( long id )
    {
        return cropDao.getCropById( id );
    }

    public final Crop getCrop( long imageid, Fraction ratio, int targetWidth, int version )
    {
        CropSelector selector = CropSelector.uniqueCrop( imageid,  ratio, targetWidth, version );

        List<Crop> crops = cropDao.getCrops( selector );

        if( crops.size() == 0 ) {
            return null;
        }

        return crops.get( 0 );
    }


    public final void saveCrop( Crop crop )
    {
		if ( crop.getId() > 0 ) {
			cropDao.updateCrop( crop );
		} else {
			cropDao.insertCrop( crop );
		}
    }

	public final void deleteCrop( Crop crop )
	{
		cropDao.deleteCrop( crop.getId() );
	}


    public final Integer getMaxVersion( long imageId )
    {
        return cropDao.getMaxVersion( imageId );
    }
}
