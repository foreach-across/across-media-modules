package com.foreach.imageserver.admin.controllers;

import com.foreach.imageserver.admin.editors.FractionEditor;
import com.foreach.imageserver.admin.models.FormatModels;
import com.foreach.imageserver.services.paths.ImageSpecifier;
import com.foreach.imageserver.admin.models.CropUploadModel;
import com.foreach.imageserver.admin.security.AuthenticationModel;
import com.foreach.imageserver.services.paths.ImagePathBuilder;
import com.foreach.imageserver.business.geometry.Rect;
import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.image.Crop;
import com.foreach.imageserver.business.image.Format;
import com.foreach.imageserver.business.image.ServableImage;
import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.business.math.Fraction;
import com.foreach.imageserver.business.taxonomy.Group;
import com.foreach.imageserver.rendering.CroppingLogic;
import com.foreach.imageserver.services.GroupService;
import com.foreach.imageserver.services.ImageService;
import com.foreach.imageserver.services.crop.CropMatcher;
import com.foreach.imageserver.services.crop.CropService;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CropController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private CropService cropService;

    @Autowired
    private CropMatcher cropMatcher;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ImagePathBuilder pathBuilder;


    private static final int MAX_DISPLAY_WIDTH = 400;

	private static final Fraction CROP_TOLERANCE = new Fraction( 5, 100 );

    @InitBinder
    protected final void initBinder( HttpServletRequest request, ServletRequestDataBinder binder )
    {
        binder.registerCustomEditor( Fraction.class, new FractionEditor() );
    }

    @RequestMapping( value="/api/crop/{encodedBasePath}", method = RequestMethod.GET )
    public final ModelAndView cropClient(
            @PathVariable String encodedBasePath )
    {
	    ServableImageData image = imageService.getImageById( decodeImageId( encodedBasePath ) );
	    Fraction defaultAspectRatio = allAspectRatios( image ).get( 0 );

	    return form( image, defaultAspectRatio, 0, 0, true, AuthenticationModel.PASSWORD );
    }

	@RequestMapping( value="/api/crop/{encodedBasePath}/version/{version}", method = RequestMethod.GET )
	public final ModelAndView cropClient(
	        @PathVariable String encodedBasePath,
	        @PathVariable int version
	)
	{
		ServableImageData image = imageService.getImageById( decodeImageId( encodedBasePath ) );
		Fraction defaultAspectRatio = allAspectRatios( image ).get( 0 );

		return form( image, defaultAspectRatio, 0, version, false, AuthenticationModel.PASSWORD );
	}



	@RequestMapping( value="/api/{encodedBasePath}/crop/create/{aspectRatio}/{targetWidth}/{version}/{versionSwitch}", method = RequestMethod.GET )
	public final ModelAndView cropClientWithRatioWidthAndVersion(
	        @PathVariable String encodedBasePath,
	        @PathVariable Fraction aspectRatio,
	        @PathVariable int targetWidth,
	        @PathVariable int version,
	        @PathVariable boolean versionSwitch
	) {
	    ServableImageData image = imageService.getImageById( decodeImageId( encodedBasePath ) );
	    return form( image, aspectRatio, targetWidth, version, versionSwitch, AuthenticationModel.PASSWORD  );
	}

	@RequestMapping( value="/api/{encodedBasePath}/crop/create/{aspectRatio}", method = RequestMethod.POST )
	public final ModelAndView updateCropFromClient(
	        @PathVariable String encodedBasePath,
	        @PathVariable Fraction aspectRatio,
	        @ModelAttribute CropUploadModel model
	) {
	    return updateCrop( decodeImageId( encodedBasePath ), aspectRatio, 0, 0, model, AuthenticationModel.PASSWORD  );
	}


	@RequestMapping( value="/api/{encodedBasePath}/crop/create/{aspectRatio}/{targetWidth}", method = RequestMethod.POST )
	public final ModelAndView updateCropFromClient(
	        @PathVariable String encodedBasePath,
	        @PathVariable Fraction aspectRatio,
	        @PathVariable int targetWidth,
	        @ModelAttribute CropUploadModel model
	) {
	    return updateCrop( decodeImageId( encodedBasePath ), aspectRatio, targetWidth, 0, model, AuthenticationModel.PASSWORD );
	}


	@RequestMapping( value="/api/{encodedBasePath}/crop/create/{aspectRatio}/{targetWidth}/{version}", method = RequestMethod.POST )
	public final ModelAndView updateCropFromClient(
	        @PathVariable String encodedBasePath,
	        @PathVariable Fraction aspectRatio,
	        @PathVariable int targetWidth,
	        @PathVariable int version,
	        @ModelAttribute CropUploadModel model
	)
	{
		return updateCrop( decodeImageId( encodedBasePath ), aspectRatio, targetWidth, version, model, AuthenticationModel.PASSWORD );
	}


    @RequestMapping( value="/image/{imageId}/crop/create", method = RequestMethod.GET )
    public final ModelAndView crop(
            @PathVariable long imageId,
            AuthenticationModel authenticationModel
    ) {
        ServableImageData image = imageService.getImageById( imageId );
        //TODO-pjs(20110707): have a "No Formats Defined" error.
        Fraction defaultAspectRatio = this.allAspectRatios( image ).get( 0 );

	    return form( image, defaultAspectRatio, 0, 0, true, authenticationModel);
    }


    @RequestMapping( value="/image/{imageId}/crop/create/{aspectRatio}", method = RequestMethod.GET )
    public final ModelAndView cropWithRatio(
            @PathVariable long imageId,
            @PathVariable Fraction aspectRatio
    ) {
        ServableImageData image = imageService.getImageById( imageId );
        return form(image, aspectRatio, 0, 0, true, null );
    }

	@RequestMapping( value="/image/{imageId}/crop/create/version/{version}", method = RequestMethod.GET )
	public final ModelAndView cropWithVersion(
	        @PathVariable long imageId,
	        @PathVariable int version,
	        AuthenticationModel authenticationModel
	) {
	    ServableImageData image = imageService.getImageById( imageId );
		Fraction defaultAspectRatio = allAspectRatios( image ).get( 0 );

	    return form( image, defaultAspectRatio, 0, version, true, authenticationModel );
	}


    @RequestMapping( value="/image/{imageId}/crop/create/{aspectRatio}/{targetWidth}", method = RequestMethod.GET )
    public final ModelAndView cropWithRatioAndWidth(
            @PathVariable long imageId,
            @PathVariable Fraction aspectRatio,
            @PathVariable int targetWidth
    ) {
        ServableImageData image = imageService.getImageById(imageId);
        return form( image, aspectRatio, targetWidth, 0, true, null );
    }

    @RequestMapping( value="/image/{imageId}/crop/create/{aspectRatio}/{targetWidth}/{version}/{versionSwitch}", method = RequestMethod.GET )
    public final ModelAndView cropWithRatioWidthAndVersion(
            @PathVariable long imageId,
            @PathVariable Fraction aspectRatio,
            @PathVariable int targetWidth,
            @PathVariable int version,
            @PathVariable boolean versionSwitch
    ) {
        ServableImageData image = imageService.getImageById(imageId);
        return form( image, aspectRatio, targetWidth, version, versionSwitch, null );
    }


    private ModelAndView form(
            ServableImageData image,
            Fraction ratio,
            int targetWidth,
            int version,
            boolean versionSwitch,
            AuthenticationModel authenticationModel)
    {
        ModelAndView mav = new ModelAndView( "crop/edit" );

        Size originalSize = new Size( image.getWidth(), image.getHeight() );
        Size displaySize = originalSize.scaleIfWider( MAX_DISPLAY_WIDTH );
        mav.addObject( "displaySize", displaySize );
        mav.addObject( "imageUrl", createImageUrl( image, originalSize, displaySize ) );

        List<Format> formats = allFormatsForRatio( image, ratio );
        List<Fraction> ratios = allAspectRatios( image );

        mav.addObject( "aspectRatioCount", ratios.size() );
        mav.addObject( "aspectRatio", ratio );

        int ix = ratios.indexOf( ratio );

        mav.addObject( "aspectRatioIndex", ix );

        if( ix > 0 ) {
            mav.addObject( "prevRatio", ratios.get( ix - 1 ) );
        }
        if( ix < ratios.size() - 1 ) {
            mav.addObject( "nextRatio", ratios.get( ix + 1 ) );
        }

        mav.addObject( "formats", new FormatModels( formats, targetWidth) );

        Crop crop = getCrop( image, ratio, targetWidth, version);

        // If the crop isn't an exist match for what we asked, we have to set the cropId to zero,
        // to force an insert instead of an update when performing a save.

        if ( ( crop.getTargetWidth() != targetWidth ) || ( crop.getVersion() != version ) ) {
            crop.setId( 0 );
        }

        Rect rect = rectFromCrop( crop, displaySize.relativeSizeUnchecked( originalSize ) );
        mav.addObject( "rect", rect );

        mav.addObject( "crop", crop );
        mav.addObject( "image", image );

        mav.addObject( "targetWidth", targetWidth );

        mav.addObject( "minWidth", ( targetWidth==0 )? 50  : targetWidth );

	    mav.addObject( "versionSwitch", versionSwitch );
        mav.addObject( "version", version );
        mav.addObject( "versions", versions( image ) );

	    mav.addObject( "baseActionUrl", baseUrlFor( image, authenticationModel ));
	    mav.addObject( "fullActionUrl", urlFor( image, ratio, targetWidth, version, null, authenticationModel ));

        return mav;
    }

	private String createImageUrl( ServableImageData image, Size originalSize, Size displaySize )
	{
		ImageSpecifier imageSpecifier = new ImageSpecifier();

		if( !displaySize.equals( originalSize ) ) {
		    imageSpecifier.setWidth( displaySize.getWidth() );
		    imageSpecifier.setHeight( displaySize.getHeight() );
		}

		return pathBuilder.createUrlPath( image, imageSpecifier );
	}

    private List<Integer> versions( ServableImage image )
    {
        List<Integer> result = new ArrayList<Integer>();
        result.add( 0 );

        Integer max = cropService.getMaxVersion( image.getId() );
        if( max != null ) {
            for( int i = 1; i <= max + 1; i++ ) {
                result.add( i );
            }
        }


        return result;
    }

    private Crop getCrop( ServableImageData image, Fraction ratio, int targetWidth, int version)
    {
        Crop crop = cropMatcher.bestCropFrom( image.getCrops(), version, ratio, targetWidth );

        if ( crop == null ) {
            Size originalSize = new Size( image.getWidth(), image.getHeight() );

            crop = new Crop();
            crop.setImageId( image.getId() );
            crop.setCropRect( CroppingLogic.calculateCropRect( originalSize, ratio ) );
        }

        return crop;
    }

    private List<Fraction> allAspectRatios( ServableImageData image )
    {
        Group group = groupService.getGroupById( image.getGroupId() );
        return group.getAllAspectRatios();
    }

    private List<Format> allFormatsForRatio( ServableImageData image, Fraction ratio )
    {
        Group group = groupService.getGroupById(image.getGroupId());
        List<Format> formats = group.getFormats();
	    List<Format> result = new ArrayList<Format>();

	    for( Format format : formats ) {
	        if( format.hasAspectRatio() && format.getAspectRatio().equals( ratio ) ) {
	            result.add( format );
	        }
	    }

	   return result;
    }

    private Rect rectFromCrop( Crop crop, Fraction scale )
    {
        Rect rect = crop.getCropRect();

        if( !scale.equals( Fraction.ONE ) ) {
            rect = rect.scaleBy( scale );
        }

        return rect;
    }

    @RequestMapping( value="/image/{imageId}/crop/create/{aspectRatio}", method = RequestMethod.POST )
    public final ModelAndView updateCrop(
            @PathVariable long imageId,
            @PathVariable Fraction aspectRatio,
            @ModelAttribute CropUploadModel model
    ) {
        return updateCrop( imageId, aspectRatio, 0, 0, model, null );
    }


    @RequestMapping( value="/image/{imageId}/crop/create/{aspectRatio}/{targetWidth}", method = RequestMethod.POST )
    public final ModelAndView updateCrop(
            @PathVariable long imageId,
            @PathVariable Fraction aspectRatio,
            @PathVariable int targetWidth,
            @ModelAttribute CropUploadModel model
    ) {
        return updateCrop( imageId, aspectRatio, targetWidth, 0, model, null );
    }


    @RequestMapping( value="/image/{imageId}/crop/create/{aspectRatio}/{targetWidth}/{version}", method = RequestMethod.POST )
    public final ModelAndView updateCrop(
            @PathVariable long imageId,
            @PathVariable Fraction aspectRatio,
            @PathVariable int targetWidth,
            @PathVariable int version,
            @ModelAttribute CropUploadModel model,
            AuthenticationModel authenticationModel
    )
    {
        ServableImageData image = imageService.getImageById( model.getImageId() );

	    // The cropping interface scales the image if it is wider than 400 pixels.,
	    // so we may need to scale the cropRect here.
        Size originalSize = image.getSize();
	    Rect rect = new Rect( model.getLeft(), model.getTop(), model.getWidth(), model.getHeight() );
	    Rect cropRect = scaleRectToLocalCoordinates( originalSize, rect );

		// Perform some validation before saving
	    if( validateCroprect( cropRect, aspectRatio, originalSize ) ) {
		    saveCrop( model.getImageId(), model.getCropId(), cropRect, aspectRatio, targetWidth, version );
	    }

	    boolean switchVersions = !model.isFixedVersion();
        return redirectFor( image, aspectRatio, targetWidth, version, switchVersions, authenticationModel );
    }

	private Rect scaleRectToLocalCoordinates( Size imageSize, Rect rect )
	{
		Size displaySize = imageSize.scaleIfWider( MAX_DISPLAY_WIDTH );

		Fraction scaleFactor = Fraction.ONE;

		if( !displaySize.equals( imageSize ) ) {
		    scaleFactor = imageSize.relativeSizeUnchecked( displaySize );
		}

		return rect.scaleBy( scaleFactor );
	}

	private boolean validateCroprect( Rect cropRect, Fraction aspectRatio, Size imageSize )
	{
		return validatAspectRatioTolerance( cropRect, aspectRatio ) && validateBounds( cropRect, imageSize ) ;
	}

	private boolean validatAspectRatioTolerance( Rect cropRect, Fraction aspectRatio)
	{
		return new Fraction( cropRect.getWidth(), cropRect.getHeight() ).withinTolerance( aspectRatio, CROP_TOLERANCE );
	}

	private boolean validateBounds( Rect cropRect, Size imageSize )
	{
		return cropRect.withinRect( new Rect( imageSize ) );
	}



	private String baseUrlFor( ServableImageData image, AuthenticationModel authenticationModel )
	{
		String redirectedPath;

		if( authenticationModel == null ) {
			redirectedPath = new StringBuffer()
					.append( "/image/" )
					.append( image.getId() )
					.append( "/crop/create" )
					.toString();
		} else {
			redirectedPath = new StringBuffer()
					.append( "/api/" )
					.append( encodeImageId( image ) )
					.append( "/crop/create" )
					.toString();
		}

		return redirectedPath;
	}


	private String urlFor( ServableImageData image, Fraction aspectRatio,
                                      int targetWidth, int version, Boolean switchVersions, AuthenticationModel authenticationModel )
	{
		StringBuffer sb = new StringBuffer()
				.append( baseUrlFor( image, authenticationModel )  )
				.append( "/" )
				.append( aspectRatio.getStringForUrl() )
				.append( "/" )
				.append( Integer.toString( targetWidth ) )
				.append( "/" )
				.append( Integer.toString( version ) );

		if( switchVersions != null ) {
			sb.append( "/" )
				.append( Boolean.toString( switchVersions ) );
		}

		return sb.toString();
	}

    private ModelAndView redirectFor( ServableImageData image, Fraction aspectRatio,
                                      int targetWidth, int version, boolean switchVersions, AuthenticationModel authenticationModel )
    {
        return new ModelAndView( "redirect:" + urlFor( image, aspectRatio, targetWidth, version, switchVersions, authenticationModel) );
    }

    private Crop saveCrop(
		    long imageId,
		    long cropId,
		    Rect cropRect,
		    Fraction aspectRatio,
		    int targetWidth,
		    int version
    )
    {
        Crop crop = new Crop();

        // We only set the parameters in common by the two crops
        crop.setImageId( imageId );
        crop.setRatioWidth( aspectRatio.getNumerator() );
        crop.setRatioHeight( aspectRatio.getDenominator() );
        crop.setCropRect( cropRect );
        crop.setTargetWidth( targetWidth );

        //if version differs from zero, duplicate the record if version zero does not yet exist

        if( version != 0 ) {
            Crop cropZero  = cropService.getCrop( crop.getImageId(), crop.getAspectRatio(), crop.getTargetWidth(), 0 );
            if ( cropZero == null ) {
                crop.setVersion( 0 );
                cropService.saveCrop( crop );
            }
        }

        // this can be an insert or update
        crop.setId( cropId );
        crop.setVersion( version );
        cropService.saveCrop( crop );

        return crop;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
	public static class NotFoundException extends RuntimeException
	{
	}

	private String encodeImageId( ServableImageData image )
	{
		String remoteId = pathBuilder.createRemoteId( image );
		Base64 base64 = new Base64( true );
		int ix = remoteId.lastIndexOf( '.' );
		String basePath = remoteId.substring( 0, ix );
		return base64.encodeToString(basePath.getBytes()).replace("\n","").replace("\r","");
	}


	private long decodeImageId( String encoded )
	{
		Base64 base64 = new Base64( true );
		String basePath = new String(base64.decode(encoded));
		String imageIdString = basePath.substring(basePath.lastIndexOf('/') + 1);

		try {
		    return Long.parseLong( imageIdString );
		} catch (NumberFormatException e) {
		    return 0L;
		}
	}
}
