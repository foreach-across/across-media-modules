package com.foreach.imageserver.admin.controllers;

import com.foreach.imageserver.services.paths.ImageSpecifier;
import com.foreach.imageserver.services.paths.ImageVersion;
import com.foreach.imageserver.services.paths.ImagePathBuilder;
import com.foreach.imageserver.services.paths.ImagePathBuilderImpl;
import com.foreach.imageserver.business.image.ServableImageData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestPathBuilder {

    private ImagePathBuilder pathBuilder;

    @Before
    public void setup()
    {
        pathBuilder = new ImagePathBuilderImpl();
    }

    @Test
    public void sanityTest()
    {
        int applicationId = 2;
        int groupId = 2;
        int imageId = 1001;
        String extension = "zod";

        ServableImageData data = new ServableImageData();
        data.setApplicationId( applicationId );
        data.setGroupId( groupId );
        data.setPath( "1970/01/01" );
        data.setId( imageId );
        data.setExtension( extension );

        ImageSpecifier imageSpecifier = new ImageSpecifier();
        imageSpecifier.setImageId( imageId );
        imageSpecifier.setFileType( extension );

        String manualPathToOriginalFile =
                pathBuilder.createManualImagePath( ImageVersion.ORIGINAL, applicationId, groupId, "1970", "01",
                                                   "01", imageSpecifier );
        String generatedPathToOriginalFile = pathBuilder.generateOriginalImagePath( data );

        Assert.assertEquals( manualPathToOriginalFile, generatedPathToOriginalFile );
    }
}
