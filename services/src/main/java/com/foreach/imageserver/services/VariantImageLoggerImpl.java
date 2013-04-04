package com.foreach.imageserver.services;

import com.foreach.imageserver.business.container.CircularArrayList;
import com.foreach.imageserver.business.container.CircularArrayListImpl;
import com.foreach.imageserver.business.image.VariantImage;
import com.foreach.imageserver.dao.VariantImageDao;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class VariantImageLoggerImpl implements VariantImageLogger
{

    @Autowired
    private VariantImageDao variantImageDao;

    private CircularArrayList<VariantImage> variantImages;
    private int capacity;

    public VariantImageLoggerImpl( int capacity ) {
        this.capacity = capacity;
        this.variantImages = new CircularArrayListImpl<VariantImage>( capacity );
    }

    public final int getCapacity() {
        return capacity;
    }

    public final void logVariantImage( VariantImage variantImage ) {
        variantImage.setLastCalled( new Date() );
        variantImages.push( variantImage );
    }

    public final void flushLog() {

        List<VariantImage> flushes = variantImages.popAll();
        for (VariantImage image : flushes) {
            variantImageDao.updateVariantImageDate(image);
        }
    }
}
