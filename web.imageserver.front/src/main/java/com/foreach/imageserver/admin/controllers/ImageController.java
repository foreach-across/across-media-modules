package com.foreach.imageserver.admin.controllers;

import com.foreach.imageserver.services.paths.ImagePathBuilder;
import com.foreach.imageserver.services.paths.ImageSpecifier;
import com.foreach.imageserver.admin.models.ImageDetailsModel;
import com.foreach.imageserver.business.image.Crop;
import com.foreach.imageserver.business.image.Format;
import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.business.Application;
import com.foreach.imageserver.business.taxonomy.Group;
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

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Controller
@RequestMapping(value = "/image")
public class ImageController
{
    @Autowired
    private ImagePathBuilder pathBuilder;
    
    @Autowired
    private ImageService imageService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private FormatService formatService;

	@RequestMapping(value = "/list")
	public final ModelAndView viewImageList()
	{
		ModelAndView mav = new ModelAndView( "image/list" );

		List<ServableImageData> images = imageService.getAllImages();

		mav.addObject( "images", filterOutImagesWithBadPath( images ) );

		return mav;
	}

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public final ModelAndView getImageDetailsPage(
            @PathVariable("id") Integer imageId
    )
    {
        ServableImageData image = imageService.getImageById( imageId );
        Application application = applicationService.getApplicationById(image.getApplicationId());
        Group group = groupService.getGroupById( image.getGroupId() );
        List<Format> formats = formatService.getFormatsByGroupId( group.getId() );

        ImageDetailsModel model = new ImageDetailsModel();
        model.setApplication( application );
        model.setGroup( group );
        model.setImage( image );
        model.setFormats( formats );

        ImageSpecifier imageSpecifier = new ImageSpecifier();
        imageSpecifier.setWidth( 64 );
        imageSpecifier.setHeight( 64 );

        model.setImagePath( pathBuilder.createUrlPath( image, imageSpecifier ) );
        model.setOriginalPath(pathBuilder.createUrlPath(image));

        ModelAndView mav = new ModelAndView("image/details");

        mav.addObject( "model", model );

        return mav;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public final ModelAndView redirectToNewCropPage(
            @PathVariable("id") Integer imageId,
            HttpServletRequest request
    ) {
        StringBuilder redirectURL = new StringBuilder();
        redirectURL.append("redirect:/image/");
        redirectURL.append(imageId);
        redirectURL.append("/crop/create/");
        String format = request.getParameter("selectedFormat");
        redirectURL.append(format);


        ModelAndView mav = new ModelAndView(redirectURL.toString());

        Crop crop = new Crop();
        crop.setImageId( imageId );

        mav.addObject( "crop", crop );

        return mav;
    }

	private List<ServableImageData> filterOutImagesWithBadPath( List<ServableImageData> images ){
		List<ServableImageData> filteredImages = new ArrayList<ServableImageData>( );

		Pattern p = Pattern.compile("^(\\d{4}/\\d{2}/\\d{2})$");

		for ( ServableImageData image : images){
			String path = image.getPath();
			if ( ( path != null) && p.matcher(path).matches()){
				filteredImages.add( image );
			}
		}
		return filteredImages;
	}
}
