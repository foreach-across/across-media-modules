package com.foreach.imageserver.example.front;

import com.foreach.imageserver.api.models.ImageModel;
import com.foreach.imageserver.api.models.ImageServerUploadResult;
import com.foreach.imageserver.api.services.ImageServerConnection;
import com.foreach.imageserver.api.services.ImageServerConnectionImpl;
import com.foreach.imageserver.example.models.UploadModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.net.URLEncoder;

@Controller
public class APIController
{
	private String appUrl;
	private String authKey;
	private String managementUrl;

	public final void setAppUrl( String appUrl )
	{
		this.appUrl = appUrl;
	}

	public final void setAuthKey( String authKey )
	{
		this.authKey = authKey;
	}

	public final void setManagementUrl( String managementUrl )
	{
		this.managementUrl = managementUrl;
	}

	@RequestMapping( value= "/", method = RequestMethod.GET )
    public final ModelAndView getClientDemoPage( @ModelAttribute("model") UploadModel model )
    {
        ModelAndView mav = new ModelAndView("client/application");

        model.setApplicationId( 1 );
        model.setGroupId( 2 );
	    model.setImageId( "" );

        return mav;
    }

    @RequestMapping( value = "/", method = RequestMethod.POST )
    public final ModelAndView postImage( @ModelAttribute("model") UploadModel image)
    {
	    ImageServerConnection connection = getConnection( image.getApplicationId(), image.getGroupId() );

	    ImageModel imageModel = new ImageModel();
	    imageModel.setImageData( image.getImageData() );

	    ImageServerUploadResult result;

	    if( image.getImageId().isEmpty() ) {

            result = connection.uploadImage( imageModel );
	    }
	    else {
			result = connection.replaceImage( image.getImageId(), imageModel );
	    }

	    image.setImageId(result.getImageId());

        String path = null;
	    String cropUrl = null;

	    if( result.getImageId() != null ) {
	        connection.getImageUrl( result.getImageId(), 64, 64 );
            cropUrl = connection.getImageCropUrl(result.getImageId());
	    }

	    ModelAndView mav;

	    if( result.getStatus().isFailure() ) {
			mav = new ModelAndView("client/application");

			mav.addObject( "model", image);
			mav.addObject( "imagePath", path );
			mav.addObject( "cropUrl", cropUrl );
			mav.addObject( "status", result.getStatus() );
	    } else {
		    String redirectedPath = new StringBuffer()
		            .append( "/" )
		            .append( image.getApplicationId() )
		            .append( "/" )
		            .append( image.getGroupId() )
				    .append( "/show?imageId=" )
		            .append( URLEncoder.encode( result.getImageId() ) )
		            .toString();

		    return new ModelAndView( "redirect:" + redirectedPath );
	    }

        return mav;
    }


	@RequestMapping( value= "/{appId}/{groupId}/show", method = RequestMethod.GET )
	public final ModelAndView showImage(
			@PathVariable("appId") int appId,
			@PathVariable("groupId") int groupId,
			@RequestParam("imageId") String imageId
	)
	{
		ImageServerConnection connection = getConnection( appId, groupId );

		ModelAndView mav = new ModelAndView("client/image");

		String path = connection.getImageUrl( imageId, 64, 64 );
	    String cropUrl = connection.getImageCropUrl( imageId );
		String cropUrl2 = connection.getImageCropUrl( imageId, 2 );
		String fullPath = connection.getImageUrl( imageId );

		mav.addObject( "imageId", imageId );
		mav.addObject( "imagePath", path );
		mav.addObject( "fullImagePath", fullPath );
		mav.addObject( "cropUrl", cropUrl );
		mav.addObject( "cropUrl2", cropUrl2 );
		return mav;
	}

	private ImageServerConnection getConnection( int applicationId, int groupId )
	{
		return new ImageServerConnectionImpl( appUrl, applicationId, groupId, managementUrl, authKey );
	}
}