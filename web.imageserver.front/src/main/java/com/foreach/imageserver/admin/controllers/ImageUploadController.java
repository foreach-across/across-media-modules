package com.foreach.imageserver.admin.controllers;

import com.foreach.imageserver.api.business.UploadStatus;
import com.foreach.imageserver.services.ImageStoreService;
import com.foreach.imageserver.services.paths.ImagePathBuilder;
import com.foreach.imageserver.services.paths.ImageSpecifier;
import com.foreach.imageserver.admin.models.ImageUploadModel;
import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.business.image.VariantImage;
import com.foreach.imageserver.rendering.ImageResizer;
import com.foreach.imageserver.services.ImageService;
import com.foreach.imageserver.services.UserGroupService;
import com.foreach.imageserver.services.VariantImageService;
import com.foreach.imageserver.services.utils.TempFileService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * This is the controller that handles the uploading of images,
 * storing them on disk and creates the needed database entry.
 */
@Controller
public class ImageUploadController
{

	private static final Logger LOG = Logger.getLogger( ImageUploadController.class );

	@Autowired
	private ImagePathBuilder pathBuilder;

	@Autowired
	private ImageResizer imageMagick;

	@Autowired
	private ImageService imageService;

	@Autowired
	private VariantImageService variantService;

	@Autowired
	private TempFileService tempFileService;

	@Autowired
	private UserGroupService userGroupService;

	@Autowired
	private ImageStoreService imageStoreService;

	private static final String APPLICATION_MARKER  = "appId";
	private static final String GROUP_MARKER        = "groupId";

	private static final String MODEL_NAME = "model";


	@RequestMapping(value = "/application/{appId}/group/{groupId}/upload", method = RequestMethod.POST)
	public final ModelAndView uploadImage(
            @PathVariable( APPLICATION_MARKER ) int applicationId,
            @PathVariable( GROUP_MARKER ) int groupId,
            @ModelAttribute( MODEL_NAME ) ImageUploadModel model)
	{
		return uploadImage( applicationId, groupId, false, model, false );
	}

	@RequestMapping(value = "/application/{appId}/group/{groupId}/upload/{txtFlag}", method = RequestMethod.POST)
	public final ModelAndView uploadImage(
			@PathVariable( APPLICATION_MARKER ) int applicationId,
			@PathVariable( GROUP_MARKER ) int groupId,
			@PathVariable( "txtFlag" ) boolean txtFlag,
			@ModelAttribute( MODEL_NAME ) ImageUploadModel model,
            boolean authenticate)
	{
        if ( authenticate && (
			( StringUtils.isBlank( model.getUserKey() ) ) ||
			( !userGroupService.isKeyForGroup( groupId, model.getUserKey() ) )
		) ) {
			return statusResponse(applicationId, groupId, UploadStatus.AUTHENTICATION_ERROR, model, txtFlag);
		}

		if ( model.getImage().isEmpty() ) {
			return statusResponse( applicationId, groupId, UploadStatus.CONTENT_ERROR, model, txtFlag );
		}

		boolean update = ( ( model.getImageKey() != null)  && ( !model.getImageKey().isEmpty() ) );

		ServableImageData image = null;

		if ( update ) {

			image = getPreviousImage( model.getImageKey() );

			if ( image == null ) {
				return statusResponse( applicationId, groupId, UploadStatus.IMAGE_NOT_FOUND_ERROR, model, txtFlag );
			}

			if ( ( image.getApplicationId() != applicationId ) || ( image.getGroupId() != groupId ) ) {
				return statusResponse( applicationId, groupId, UploadStatus.AUTHENTICATION_ERROR, model, txtFlag );
			}
		}

		Size imageSize = getImageSize( model );
		if ( imageSize == null ) {
			return statusResponse( applicationId, groupId, UploadStatus.SERVER_ERROR, model, txtFlag );
		}


		if ( update ) {
			try {
				image = updateImage( image, model, imageSize );
			} catch (Exception e ) {
				LOG.error( "failed to update image", e);
			}
		}
		else {
			image = saveNewImage( applicationId, groupId, model, imageSize );
		}

		if ( txtFlag ) {
			ModelAndView mav = new ModelAndView( "plain/uploadResponse" );
			mav.addObject( "status", UploadStatus.SUCCES );
			mav.addObject( "imageId", pathBuilder.createRemoteId( image ) );
			return mav;
		}
		else {
			return new ModelAndView(
					"redirect:/application/" + applicationId + "/group/" + groupId + "?uploadedOk=true" );
		}
	}

	private ServableImageData getPreviousImage( String remoteId )
	{
		long imageId = pathBuilder.imageIdFromRemoteId( remoteId );

		if ( imageId == 0 ) {
			return null;
		}

		return imageService.getImageById( imageId );
	}

	@RequestMapping(value = "/application/{appId}/group/{groupId}/delete/{txtFlag}", method = RequestMethod.POST)
	public final ModelAndView deleteImage(
			@PathVariable( APPLICATION_MARKER ) int applicationId,
			@PathVariable( GROUP_MARKER ) int groupId,
			@PathVariable("txtFlag") boolean txtFlag,
			@ModelAttribute( MODEL_NAME ) ImageUploadModel model,
            boolean authenticate)
	{
        if ( authenticate && (
			( StringUtils.isBlank( model.getUserKey() ) ) ||
			( !userGroupService.isKeyForGroup( groupId, model.getUserKey() ) )
		) ) {
			return statusResponse(applicationId, groupId, UploadStatus.AUTHENTICATION_ERROR, model, txtFlag);
		}

		ServableImageData image = null;

		long imageId = pathBuilder.imageIdFromRemoteId( model.getImageKey() );

		if ( imageId == 0 ) {
			return statusResponse( applicationId, groupId, UploadStatus.IMAGE_NOT_FOUND_ERROR, model, txtFlag );
		}

		image = imageService.getImageById( imageId );

		if ( ( image.getApplicationId() != applicationId ) || ( image.getGroupId() != groupId ) ) {
			return statusResponse( applicationId, groupId, UploadStatus.AUTHENTICATION_ERROR, model, txtFlag );
		}

		deleteImage( image );

		if ( txtFlag ) {
			ModelAndView mav = new ModelAndView( "plain/uploadResponse" );
			mav.addObject( "status", UploadStatus.SUCCES );
			return mav;
		}
		else {
			return new ModelAndView(
					"redirect:/application/" + applicationId + "/group/" + groupId + "?uploadedOk=true" );
		}
	}



	private ServableImageData saveNewImage( int applicationId, int groupId, ImageUploadModel model, Size imageSize )
	{
		ServableImageData image = new ServableImageData();
		image.setApplicationId( applicationId );
		image.setGroupId( groupId );
		image.setFileSize( model.getImage().getSize() );
		image.setWidth( imageSize.getWidth() );
		image.setHeight( imageSize.getHeight() );

		String name = model.getOriginalFilename();
		if( name == null ) {
			name = model.getImage().getOriginalFilename();
		}
		String extension = getExtension( name );
		if ( StringUtils.equalsIgnoreCase( extension, "jpeg" ) ) {
			extension = "jpg";
		}

		image.setExtension( extension );
		image.setPath( pathBuilder.createChildPath( new Date() ) );

		image.setOriginalFileName( model.getOriginalFilename() );

		imageService.saveImage( image );

		imageStoreService.saveImage( image, model.getImage() );

		return image;
	}

	private ServableImageData updateImage( ServableImageData image, ImageUploadModel model, Size imageSize )
	{
		image.setFileSize( model.getImage().getSize() );
		image.setWidth(imageSize.getWidth());
		image.setHeight(imageSize.getHeight());

		String extension = getExtension( model.getOriginalFilename() );
		if ( StringUtils.equalsIgnoreCase( extension, "jpeg" ) ) {
			extension = "jpg";
		}

		String oldExtension = image.getExtension();

		image.setExtension(extension);
		image.setOriginalFileName( model.getOriginalFilename() );

		// deletes variant records
		deleteAllVariantsFor(image);

		LOG.debug( "replacing image record for "+image.getPath()+" "+image.getId() );

		// also deletes crop that have become invalid
		imageService.saveImage( image, true );

		LOG.debug( "replacing image on FS"+image.getPath()+" "+image.getId() );

		imageStoreService.replaceImage( image, model.getImage(), oldExtension );

		return image;
	}

	private void deleteImage( ServableImageData image )
	{
		deleteAllVariantsFor(image);

		LOG.debug( "deleting image record for "+image.getPath()+" "+image.getId() );

		imageStoreService.deleteImage( image );
	}

	private void deleteAllVariantsFor( ServableImageData image )
	{
		LOG.debug( "deleting all variants for "+image.getPath()+" "+image.getId() );

		List<VariantImage> variants = variantService.getAllVariantsForImage( image.getId() );

		ImageSpecifier specifier = new ImageSpecifier();
		specifier.setImageId(image.getId());
		specifier.setFileType( image.getExtension() );

		for( VariantImage variant : variants ) {
			specifier.setHeight( variant.getHeight() );
			specifier.setWidth( variant.getWidth() );
			specifier.setVersion( variant.getVersion() );

			String targetPath = pathBuilder.generateVariantImagePath( image, specifier );

			if ( !tempFileService.deleteFile( targetPath ) ) {
				LOG.error( "failed to delete "+targetPath );
			}

			variantService.deleteVariantImage( variant );
		}
	}

	private Size getImageSize( ImageUploadModel model )
	{
		try {
			File tempFile = tempFileService.tmpFile( "image", ".img" );
			String tempPath = tempFile.getPath();
			saveImageOnDisk( model.getImage(), tempPath );
			Size size = imageMagick.getSize( tempPath );
			tempFileService.deleteFile( tempPath );
			return size;
		}
		catch ( IOException e ) {
			LOG.error( "Exception has occured.", e );
			return null;
		}
		catch ( InterruptedException e ) {
			LOG.error( "Exception has occured.", e );
			return null;
		}
	}

	private ModelAndView statusResponse(
			int applicationId, int groupId, UploadStatus status, ImageUploadModel model, boolean txtFlag )
	{
		if ( txtFlag ) {
			ModelAndView mav = new ModelAndView( "plain/uploadResponse" );
			mav.addObject( "status", status );
			mav.addObject( "imageId", "" );
			return mav;
		}
		else {
			return uploadForm( applicationId, groupId, model, null );
		}
	}

	@RequestMapping(value = "/application/{appId}/group/{groupId}/upload", method = RequestMethod.GET)
	public final ModelAndView uploadImage(
			@PathVariable( APPLICATION_MARKER ) int applicationId,
			@PathVariable( GROUP_MARKER ) int groupId,
			UploadStatus uploadStatus )
	{
		return uploadForm( applicationId, groupId, new ImageUploadModel(), uploadStatus );
	}

	private ModelAndView uploadForm(
			int applicationId, int groupId, ImageUploadModel model, UploadStatus uploadStatus )
	{

		ModelAndView mav = new ModelAndView( "image/upload" );

		mav.addObject( "applicationId", applicationId );
		mav.addObject( "groupId", groupId );
		mav.addObject( MODEL_NAME, model );
		mav.addObject( "uploadStatus", uploadStatus );

		return mav;
	}

	private void saveImageOnDisk( MultipartFile image, String fileName )
	{
		try {
			File f = new File( fileName );
			FileUtils.writeByteArrayToFile( f, image.getBytes() );
		}
		catch ( IOException e ) {
			LOG.error( "Exception has occured.", e );
		}
	}

	private String getExtension( String filename )
	{
		if (filename == null) {
			return null;
		}
		return filename.substring( filename.lastIndexOf( '.' ) + 1 );
	}
}