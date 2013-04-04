package com.foreach.imageserver.services;

import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.image.Format;
import com.foreach.imageserver.dao.FormatDao;
import com.foreach.imageserver.dao.VariantImageDao;
import com.foreach.imageserver.dao.selectors.VariantImageSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FormatServiceImpl implements FormatService
{
    @Autowired
    private FormatDao formatDao;

    @Autowired
    private VariantImageDao variantImageDao;

    public final Format getFormatById( int id )
    {
        return formatDao.getFormatById( id );
    }

    public final List<Format> getFormatsByGroupId( int groupId )
    {
        return formatDao.getFormatsByGroupId( groupId );
    }

	public final void saveFormat( Format format ) {
		if ( format.getId() > 0 ) {
			formatDao.updateFormat( format );
		} else {
			formatDao.insertFormat( format );
		}
	}

    public final void deleteFormat( int formatId ) {
        VariantImageSelector selector = VariantImageSelector.onFormatId( formatId );

        variantImageDao.deleteVariantImages(selector);

        formatDao.deleteFormat( formatId );
    }

    public final int getFormatIdForDimension( Size size, int groupId )
    {
        List<Format> formats =  formatDao.getFormatsByGroupId( groupId );
        for ( Format format: formats ) {
			if ( format.matches( size ) ) {
			   return format.getId();
			}
		}
		return 0;
    }
}
