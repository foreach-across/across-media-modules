package com.foreach.imageserver.admin.controllers;

import com.foreach.imageserver.admin.models.FormatUploadModel;
import com.foreach.imageserver.business.image.Dimensions;
import com.foreach.imageserver.business.image.Format;
import com.foreach.imageserver.business.math.Fraction;
import com.foreach.imageserver.services.FormatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class FormatController {

    @Autowired
    private FormatService formatService;

	private static final String APPLICATION_MARKER  = "appId";
	private static final String GROUP_MARKER        = "groupId";
	private static final String FORMAT_MARKER       = "formatId";

	private static final String FORMAT_CREATE_URL_MATCHER
			= "/application/{appId}/group/{groupId}/format/create";

	private static final String FORMAT_MODIFY_URL_MATCHER
			= "/application/{appId}/group/{groupId}/format/{formatId}";


    @RequestMapping(value = FORMAT_CREATE_URL_MATCHER, method = RequestMethod.GET)
	public final ModelAndView showFormatCreatePage(
			@PathVariable( APPLICATION_MARKER ) Integer id,
            @PathVariable( GROUP_MARKER ) Integer groupId )
	{
		ModelAndView mav = new ModelAndView( "format/details" );

		mav.addObject( "formatUploadModel", new FormatUploadModel() );
        mav.addObject( "groupId", groupId );
        mav.addObject( "applicationId", id );

		return mav;
	}

	@RequestMapping( value = FORMAT_CREATE_URL_MATCHER, method = RequestMethod.POST)
	public final ModelAndView saveFormat( @ModelAttribute FormatUploadModel model,
	                          BindingResult bindingResult,
	                          @PathVariable( APPLICATION_MARKER ) int id,
	                          @PathVariable( GROUP_MARKER ) int groupId)
	{
	    return updateFormat( model, bindingResult, id, groupId, 0 );
	}


	@RequestMapping(value = FORMAT_MODIFY_URL_MATCHER, method = RequestMethod.GET)
	public final ModelAndView showFormatEditPage(
	        @PathVariable( APPLICATION_MARKER ) Integer id,
	        @PathVariable( GROUP_MARKER ) Integer groupId,
	        @PathVariable( FORMAT_MARKER ) Integer formatId,
	        @RequestParam( value = "formatSavedOk", required = false ) boolean savedOk )

	{
		Format format = formatService.getFormatById( formatId );

	    FormatUploadModel formatUploadModel = new FormatUploadModel();

	    formatUploadModel.setName(format.getName());

	    formatUploadModel.setWidth(format.getDimensions().getWidth());
	    formatUploadModel.setHeight(format.getDimensions().getHeight());

	    if (format.getDimensions().isAbsolute()) {
	       formatUploadModel.setAbsolute( true );
	       formatUploadModel.setWidth(format.getDimensions().getWidth());
	       formatUploadModel.setHeight(format.getDimensions().getHeight());
	    } else {
	       formatUploadModel.setWidth(format.getDimensions().getAspectRatio().getNumerator());
	       formatUploadModel.setHeight(format.getDimensions().getAspectRatio().getDenominator());
	    }

	    return formatEditPage(formatUploadModel, savedOk, id, groupId);
	}

    @RequestMapping( params = "save", value = FORMAT_MODIFY_URL_MATCHER, method = RequestMethod.POST )
    public final ModelAndView updateFormat( @ModelAttribute FormatUploadModel formatUploadModel,
                              BindingResult bindingResult,
                              @PathVariable( APPLICATION_MARKER ) int id,
                              @PathVariable( GROUP_MARKER ) int groupId,
                              @PathVariable( FORMAT_MARKER ) int formatId)
    {
        if ( !bindingResult.hasErrors() ) {
            Format format = new Format();
            format.setId( formatId );
            format.setName( formatUploadModel.getName() );
            format.setGroupId(groupId); // Should this be a modifiable attribute ?
            if (formatUploadModel.isAbsolute()){
                format.setDimensions( new Dimensions(formatUploadModel.getWidth(), formatUploadModel.getHeight()) );
            } else{
                format.setDimensions(new Dimensions( new Fraction(formatUploadModel.getWidth(), formatUploadModel.getHeight()) ));
            }
			formatService.saveFormat(format);

			return new ModelAndView( "redirect:" + format.getId() + "?formatSavedOk=true" );
		}
		else {
			return formatEditPage(formatUploadModel, false, id, groupId);
		}
    }


    @RequestMapping( params = "delete", value = FORMAT_MODIFY_URL_MATCHER, method = RequestMethod.POST )
    public final ModelAndView deleteFormat(
            @PathVariable( APPLICATION_MARKER ) int id,
            @PathVariable( GROUP_MARKER ) int groupId,
            @PathVariable( FORMAT_MARKER ) int formatId
    )
    {
        formatService.deleteFormat( formatId );

        return new ModelAndView("redirect:/application/" + id + "/group/" + groupId + "?formatRemovedOk=true");
    }

    private ModelAndView formatEditPage( FormatUploadModel formatUploadModel,
                                         boolean savedOk,
                                         int applicationId,
                                         int groupId)
	{
		ModelAndView mav = new ModelAndView( "format/details" );

        mav.addObject( "groupId", groupId );
        mav.addObject( "applicationId", applicationId );
        mav.addObject( "formatUploadModel", formatUploadModel );
		mav.addObject( "isSavedOk", savedOk );

		return mav;
	}

}
