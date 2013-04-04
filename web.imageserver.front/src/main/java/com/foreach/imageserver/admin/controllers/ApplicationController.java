package com.foreach.imageserver.admin.controllers;

import com.foreach.imageserver.admin.viewHelpers.ApplicationViewHelper;
import com.foreach.imageserver.admin.viewHelpers.FormatsViewHelper;
import com.foreach.imageserver.admin.viewHelpers.GroupsViewHelper;
import com.foreach.imageserver.admin.viewHelpers.ImagesViewHelper;
import com.foreach.imageserver.business.image.Format;
import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.business.taxonomy.Application;
import com.foreach.imageserver.business.taxonomy.Group;
import com.foreach.imageserver.dao.selectors.ImageSelector;
import com.foreach.imageserver.services.ApplicationService;
import com.foreach.imageserver.services.FormatService;
import com.foreach.imageserver.services.GroupService;
import com.foreach.imageserver.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping( "/application" )
public class ApplicationController
{
	@Autowired
	private ApplicationService applicationService;

	@Autowired
	private GroupService groupService;

	@Autowired
	private FormatService formatService;

	@Autowired
	private ImageService imageService;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public final ModelAndView showApplicationList()
	{
		ModelAndView mav = new ModelAndView( "application/list" );

        List<ApplicationViewHelper> applications = buildApplicationViewHelperList(applicationService.getAllApplications());

		mav.addObject( "applications", applications);
        mav.addObject( "numberOfApplications", applications.size());

		return mav;
	}

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public final ModelAndView showApplicationEditPage(
			@PathVariable("id") Integer applicationId
	)
	{
		Application application = applicationService.getApplicationById( applicationId );

		List<Group> groups = groupService.allGroupsByApplicationId( applicationId );

        GroupsViewHelper groupsViewHelper = new GroupsViewHelper(groups);

		ModelAndView mav = new ModelAndView( "application/details" );

		mav.addObject( "application", application);
		mav.addObject( "groups", groupsViewHelper);
		mav.addObject( "amountOfGroups", groups.size());

		return mav;
	}

	@RequestMapping(value = "/{id}/group/{groupId}", method = RequestMethod.GET)
	public final ModelAndView showGroupDetails(
			@PathVariable("id") Integer applicationId,
	        @PathVariable("groupId") Integer groupId)
	{
		Application application = applicationService.getApplicationById( applicationId );
		Group group = groupService.getGroupById( groupId );

		List<Format> formats = formatService.getFormatsByGroupId( groupId );
        FormatsViewHelper formatsViewHelper = new FormatsViewHelper(formats);

		ImageSelector selector = ImageSelector.onGroupId(groupId);
        List<ServableImageData> images = imageService.getImages(selector);
        ImagesViewHelper imagesViewHelper = new ImagesViewHelper(images);

        List<Group> groups = groupService.allGroupsByApplicationId( applicationId );
        GroupsViewHelper groupsViewHelper = new GroupsViewHelper(groups);

		ModelAndView mav = new ModelAndView( "application/group" );

		mav.addObject( "application", application);
		mav.addObject( "group", group);
        mav.addObject( "groups", groupsViewHelper);
		mav.addObject( "images", imagesViewHelper);
		mav.addObject( "formats", formatsViewHelper);

		return mav;
	}

     private List<ApplicationViewHelper> buildApplicationViewHelperList(List<Application> applications) {

        List<ApplicationViewHelper> viewHelperList = new ArrayList<ApplicationViewHelper>();

		for(Application application : applications){
            ImageSelector selector = ImageSelector.onApplicationId(application.getId());

            ApplicationViewHelper viewHelper = new ApplicationViewHelper(application);
			viewHelper.setNumberOfGroups(groupService.allGroupsByApplicationId(application.getId()).size());
            viewHelper.setNumberOfImages(imageService.getImageCount(selector));

            viewHelperList.add(viewHelper);
		}
		return viewHelperList;
    }
}

