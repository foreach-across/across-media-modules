package com.foreach.imageserver.admin.controllers;

import com.foreach.imageserver.services.paths.ImagePathBuilder;
import com.foreach.imageserver.business.image.Format;
import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.business.taxonomy.Group;
import com.foreach.imageserver.services.ApplicationService;
import com.foreach.imageserver.services.FormatService;
import com.foreach.imageserver.services.GroupService;
import com.foreach.imageserver.services.ImageService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static com.foreach.shared.utils.InjectUtils.inject;
import static org.mockito.Mockito.*;

public class TestImageController {

    private ImageController controller;

    private ImageService imageService;
    private ApplicationService applicationService;
    private GroupService groupService;
    private FormatService formatService;
    private ImagePathBuilder pathBuilder;

    @Before
    public void setup()
    {
        controller = new ImageController();
        imageService = mock( ImageService.class );
        applicationService = mock( ApplicationService.class );
        groupService = mock( GroupService.class );
        formatService = mock( FormatService.class );
        pathBuilder = mock( ImagePathBuilder.class );

        inject( controller, "imageService", imageService );
        inject( controller, "applicationService", applicationService );
        inject( controller, "groupService", groupService );
        inject( controller, "formatService", formatService );
        inject( controller, "pathBuilder", pathBuilder );
    }

    @Test
    public void detailPage()
    {
        int imageId = 1001;

        int imageWidth = 400;
        int imageHeight = 300;

        ServableImageData image = new ServableImageData();
        image.setId( imageId );
        image.setWidth( imageWidth );
        image.setHeight( imageHeight );

        when( imageService.getImageById( imageId ) ).thenReturn( image );
        when( groupService.getGroupById( anyInt() ) ).thenReturn( new Group() );
        when(formatService.getFormatsByGroupId(anyInt())).thenReturn( new ArrayList<Format>());

        controller.getImageDetailsPage( imageId );

        verify( imageService, times( 1 ) ).getImageById( imageId );
    }
}
