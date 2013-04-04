package com.foreach.imageserver.admin.controllers;

import com.foreach.imageserver.admin.models.ImageUploadModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "/api")
public class ApiController
{
	@Autowired
	private ImageUploadController imageUploadController;

	@RequestMapping(value = "/{appId}/{groupId}/upload", method = RequestMethod.POST)
	public final ModelAndView uploadImageFromApi(
			@PathVariable("appId") int applicationId,
			@PathVariable("groupId") int groupId,
			@ModelAttribute("model") ImageUploadModel model )
	{
		return imageUploadController.uploadImage( applicationId, groupId, true, model, true );
	}

	@RequestMapping(value = "/{appId}/{groupId}/delete", method = RequestMethod.POST)
	public final ModelAndView deleteImageFromApi(
			@PathVariable("appId") int applicationId,
			@PathVariable("groupId") int groupId,
			@ModelAttribute("model") ImageUploadModel model )
	{
		return imageUploadController.deleteImage( applicationId, groupId, true, model, true );
	}

}
