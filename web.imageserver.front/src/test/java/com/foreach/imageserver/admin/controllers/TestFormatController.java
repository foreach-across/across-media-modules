package com.foreach.imageserver.admin.controllers;

import com.foreach.imageserver.admin.models.FormatUploadModel;
import com.foreach.imageserver.business.image.Format;
import com.foreach.imageserver.services.FormatService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BindingResult;

import static com.foreach.shared.utils.InjectUtils.inject;
import static org.mockito.Mockito.*;


public class TestFormatController
{
	private FormatController controller;

	private FormatService formatService;

	private int appId = 1001;
	private int groupId = 2002;

	@Before
	public void setup()
	{
	    controller = new FormatController();
	    formatService = mock( FormatService.class );

	    inject( controller, "formatService", formatService );
	}

	@Test
	public void deleteFormat()
	{
		int formatId = 1001;

		controller.deleteFormat( appId, groupId, formatId );

		verify( formatService, times( 1 ) ).deleteFormat( formatId );
	}

	@Test
	public void updateFormat()
	{
		int formatId = 1001;

		FormatUploadModel model = new FormatUploadModel();

		BindingResult bindingResult = mock( BindingResult.class );

		controller.updateFormat( model, bindingResult, appId, groupId, formatId );

		verify( formatService, times( 1 ) ).saveFormat( (Format) anyObject() );
	}
}
