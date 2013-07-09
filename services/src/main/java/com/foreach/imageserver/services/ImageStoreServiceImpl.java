package com.foreach.imageserver.services;

import com.foreach.imageserver.business.Image;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class ImageStoreServiceImpl implements ImageStoreService
{
	private static final Logger LOG = Logger.getLogger( ImageStoreServiceImpl.class );
	private static final FastDateFormat PATH_FORMAT = FastDateFormat.getInstance( "/yyyy/MM/dd/" );

	private String originalBasePath;
	private String variantBasePath;

	public ImageStoreServiceImpl( @Value("store.original.path") String originalBasePath,
	                              @Value("store.variant.path") String variantBasePath ) {
		this.originalBasePath = originalBasePath;
		this.variantBasePath = variantBasePath;
	}

	@Override
	public String generateRelativeImagePath( Image image ) {
		return PATH_FORMAT.format( image.getDateCreated() );
	}

	@Override
	public long saveImage( Image image, InputStream imageData ) {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void deleteVariants( Image image ) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
