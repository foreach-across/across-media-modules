package com.foreach.imageserver.admin.rendering;

import com.foreach.imageserver.business.geometry.Rect;
import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.image.Crop;
import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.services.paths.ImageSpecifier;
import com.foreach.imageserver.services.paths.ImagePathBuilder;
import com.foreach.imageserver.rendering.CroppingLogic;
import com.foreach.imageserver.rendering.ImageResizer;
import com.foreach.imageserver.services.crop.CropMatcher;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ImageRenderingFacadeImpl implements ImageRenderingFacade
{
	private static final Logger LOG = Logger.getLogger(ImageRenderingFacade.class);

    @Autowired
    private ImagePathBuilder pathBuilder;

    @Autowired
    private ImageResizer imageResizer;

    @Autowired
    private CropMatcher cropMatcher;

	public final ImageRenderingResult generateVariant( ServableImageData imageData, ImageSpecifier imageSpecifier )
	{
        // Get the physical source file for the original
        String sourcePath = pathBuilder.generateOriginalImagePath( imageData );
        String targetPath = pathBuilder.generateVariantImagePath( imageData, imageSpecifier );

        Size targetSize = new Size( imageSpecifier.getWidth(), imageSpecifier.getHeight() );

		// Find the best matching crop to apply
        Crop crop = cropMatcher.bestCropFrom(imageData.getCrops(), imageSpecifier.getVersion(), targetSize);

        Rect cropRect = null;

        if( crop == null) {
            // We didn't find a crop, so just use the default cropping logic
            Size sourceSize = new Size( imageData.getWidth(), imageData.getHeight() );
            cropRect = CroppingLogic.calculateCropRect(sourceSize, targetSize.aspectRatio());
        } else {

            cropRect = crop.getCropRect();
        }

        try {
            imageResizer.resize( sourcePath, targetPath, targetSize, cropRect );
        } catch ( Exception e ) {
            LOG.error("Exception has occured.", e);
        }

		return new ImageRenderingResult( new File( targetPath ), ( crop == null)? null : crop.getId() );
	}
}