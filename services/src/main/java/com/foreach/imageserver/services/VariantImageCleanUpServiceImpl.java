package com.foreach.imageserver.services;

import com.foreach.imageserver.business.image.Format;
import com.foreach.imageserver.business.taxonomy.Application;
import com.foreach.imageserver.business.taxonomy.Group;
import org.springframework.beans.factory.annotation.Autowired;

public class VariantImageCleanUpServiceImpl implements VariantImageCleanUpService{


    //todo properly wire this
    /*
    @Autowired
    private ImagePathBuilder imagePathBuilder;
    */

    @Autowired
    private VariantImageService variantImageService;


    @Autowired
    private ImageService imageService;

    public void cleanUpVariantsForApplication(Application application) {
    }

    public void cleanUpVariantsForGroup(Group group) {
    }

    public void cleanUpVariantsForFormat(Format format) {
        /*Todo add new createManualImagePath method that takes whole date path,

        VariantImageSelector selector = VariantImageSelector.onFormatId( format.getId() );
        List<VariantImage> variantImages = variantImageService.getVariantImages( selector );

        for(VariantImage variantImage : variantImages){
            ServableImageData image = imageService.getImageById( variantImage.getImageId() );

            ImageSpecifier imageSpecifier = new ImageSpecifier();
            imageSpecifier.setFileType( image.getExtension() );
            imageSpecifier.setWidth( variantImage.getWidth() );
            imageSpecifier.setHeight(variantImage.getHeight());
            imageSpecifier.setVersion( variantImage.getVersion() );

            String physicalPath =
				imagePathBuilder.createManualImagePath(ImageType.VARIANT, image.getApplicationId(), image.getGroupId(), image.getPath(),
				                                   imageSpecifier );

		    File imageFile = new File( physicalPath );

            imageFile.delete();
        }
        */

    }
}
