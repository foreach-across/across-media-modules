package com.foreach.imageserver.admin.controllers;

import com.foreach.imageserver.services.VariantImageLogger;
import com.foreach.imageserver.services.paths.ImageSpecifier;
import com.foreach.imageserver.services.paths.ImageVersion;
import com.foreach.imageserver.admin.rendering.ImageRenderingFacade;
import com.foreach.imageserver.admin.service.ImageServerFacade;
import com.foreach.imageserver.services.paths.ImagePathBuilder;
import com.foreach.imageserver.business.image.VariantImage;
import com.foreach.imageserver.services.FormatService;
import com.foreach.imageserver.services.VariantImageService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static com.foreach.shared.utils.InjectUtils.inject;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TestImageStreamingController
{
	private ImageStreamingController controller;


	private ImagePathBuilder pathBuilder;
	private ImageServerFacade imageServerFacade;
	private ImageRenderingFacade imageRenderingFacade;
	private VariantImageService variantImageService;
    private FormatService formatService;
    private VariantImageLogger variantImageLogger;

	private int applicationId;
	private int groupId;


	@Before
	public void setup() throws IOException
	{
	    controller = new ImageStreamingController();

		pathBuilder = mock( ImagePathBuilder.class );
		imageServerFacade = mock( ImageServerFacade.class );
		imageRenderingFacade = mock( ImageRenderingFacade.class );
	    variantImageService = mock( VariantImageService.class );
	    formatService = mock( FormatService.class );
		variantImageLogger = mock( VariantImageLogger.class );

		inject( controller, "pathBuilder", pathBuilder );
		inject( controller, "imageServerFacade", imageServerFacade );
	    inject( controller, "imageRenderingFacade", imageRenderingFacade );
	    inject( controller, "variantImageService", variantImageService );
	    inject( controller, "formatService", formatService );
		inject( controller, "variantImageLogger", variantImageLogger );

		applicationId = 1001;
		groupId = 2002;

	}

	public void getImage(){
		// Image exists on disk, it is returned
	}

	public void getImageThatDoesNotExist() {
		// 404 is returned
	}

	public void getImageWithOriginal() {
		// Image doesn't exist on disk
		// Best matching crop is generated
		// Crop is returned
	}

	public void getSimultaneousCropGeneration() {
		// Image does not exist

		// Multiple thread requests are locked for the same image
		// Threads for another image are allowed

	}

	@Test
	public void accessIsLogged() throws IOException
	{
		String pathYear = "2011";
		String pathMonth = "07";
		String pathDay = "11";

		int width = 400;
		int height = 300;

		ImageSpecifier specifier = new ImageSpecifier();
		specifier.setWidth( width );
		specifier.setHeight( height );

		HttpServletResponse response = new MockHttpServletResponse();

		File file = File.createTempFile( "TestImageStreamingController", "" );
		assertEquals( true, file.exists() );

		when( pathBuilder.createManualImagePath( (ImageVersion) anyObject(), eq(applicationId), eq(groupId), eq(pathYear), eq(pathMonth),
		                                         eq(pathDay), (ImageSpecifier) anyObject() ) )
				.thenReturn( file.getPath() );

		controller.streamVariantImage( applicationId, groupId, pathYear, pathMonth, pathDay, specifier, response );

		verify( variantImageLogger ).logVariantImage( (VariantImage) anyObject());
	}
}
