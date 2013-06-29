package com.foreach.imageserver.admin.controllers;

import com.foreach.imageserver.api.business.UploadStatus;
import com.foreach.imageserver.services.ImageStoreService;
import com.foreach.imageserver.services.paths.ImagePathBuilder;
import com.foreach.imageserver.services.paths.ImageSpecifier;
import com.foreach.imageserver.services.paths.ImageVersion;
import com.foreach.imageserver.admin.models.ImageUploadModel;
import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.business.image.VariantImage;
import com.foreach.imageserver.rendering.ImageResizer;
import com.foreach.imageserver.services.ImageService;
import com.foreach.imageserver.services.UserGroupService;
import com.foreach.imageserver.services.VariantImageService;
import com.foreach.imageserver.services.utils.TempFileService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import static com.foreach.shared.utils.InjectUtils.inject;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TestImageUploadController
{
	private ImageUploadController controller;

	private ImagePathBuilder pathBuilder;
	private ImageResizer imageMagick;
	private ImageService imageService;
	private VariantImageService variantService;
	private TempFileService tempFileService;
	private UserGroupService userGroupService;
	private ImageStoreService imageStoreService;

	private int applicationId;
	private int groupId;
	private String authenticationKey;

	private File tempFile;
	private MultipartFile image;
	private byte imageContent[] = {(byte) 0x80};

	@Before
	public void setup() throws IOException
	{
	    controller = new ImageUploadController();

		pathBuilder = mock( ImagePathBuilder.class );
		imageMagick = mock( ImageResizer.class );
		imageService = mock( ImageService.class );
	    variantService = mock( VariantImageService.class );
	    tempFileService = mock( TempFileService.class );
	    userGroupService = mock( UserGroupService.class );
		imageStoreService = mock( ImageStoreService.class );

		inject( controller, "pathBuilder", pathBuilder );
		inject( controller, "imageMagick", imageMagick );
	    inject( controller, "imageService", imageService );
	    inject( controller, "variantService", variantService );
	    inject( controller, "tempFileService", tempFileService );
	    inject( controller, "userGroupService", userGroupService );
		inject( controller, "imageStoreService", imageStoreService );

		image = mock( MultipartFile.class );

		applicationId = 1001;
		groupId = 2002;

		authenticationKey = "Knockknockwhosthere";

		tempFile = File.createTempFile( "TestImageUploadController", "" );

		when( userGroupService.isKeyForGroup( anyInt(), anyString() ) ).thenReturn( false );
		when( userGroupService.isKeyForGroup( groupId, authenticationKey ) ).thenReturn( true );

		// obviously not recursive...
		when( tempFileService.tmpFile( anyString(), anyString() ) ).thenReturn( tempFile );

		when( image.getBytes() ).thenReturn( imageContent );

	}

	@After
	public void teardown()
	{
		tempFile.delete();
	}

	@Test
	public void apiUploadWithoutCredentials()
	{
		String userKey = null;
		String originalFileName = "zeOriginalFilename";
		MultipartFile image = mock( MultipartFile.class );

		ImageUploadModel uploadModel = new ImageUploadModel();
		uploadModel.setUserKey( userKey );
		uploadModel.setOriginalFilename( originalFileName );
		uploadModel.setImageKey( "" );
		uploadModel.setImage( image );

		ModelAndView mav = controller.uploadImage( applicationId, groupId, true, uploadModel, true );

		assertEquals( UploadStatus.AUTHENTICATION_ERROR, mav.getModel().get( "status" ) );
	}

	@Test
	public void apiUploadWithIncorrectCredentials()
	{
		String userKey = "zeWrongKey";
		String originalFileName = "zeOriginalFilename";
		MultipartFile image = mock( MultipartFile.class );

		ImageUploadModel uploadModel = new ImageUploadModel();
		uploadModel.setUserKey( userKey );
		uploadModel.setOriginalFilename( originalFileName );
		uploadModel.setImageKey( "" );
		uploadModel.setImage( image );

		ModelAndView mav = controller.uploadImage( applicationId, groupId, true, uploadModel, true );

		assertEquals( UploadStatus.AUTHENTICATION_ERROR, mav.getModel().get( "status" ) );
	}

	@Test
	public void apiUploadImageMagickFailure() throws IOException, InterruptedException
	{
		String userKey = authenticationKey;
		String originalFileName = "zeOriginalFilename";

		ImageUploadModel uploadModel = new ImageUploadModel();
		uploadModel.setUserKey( userKey );
		uploadModel.setOriginalFilename( originalFileName );
		uploadModel.setImageKey( "" );
		uploadModel.setImage( image );

		when( imageMagick.getSize( anyString() ) ).thenReturn( null );

		ModelAndView mav = controller.uploadImage( applicationId, groupId, true, uploadModel, true );

		assertEquals( UploadStatus.SERVER_ERROR, mav.getModel().get( "status" ) );
	}

	@Test
	public void apiUploadImage() throws IOException, InterruptedException
	{
		String userKey = authenticationKey;
		String originalFileName = "zeOriginalFilename";

		ImageUploadModel uploadModel = new ImageUploadModel();
		uploadModel.setUserKey( userKey );
		uploadModel.setOriginalFilename( originalFileName );
		uploadModel.setImageKey( "" );
		uploadModel.setImage( image );

		byte imageContent[] = {(byte) 0x80, (byte) 0x81};

		when( image.getBytes() ).thenReturn( imageContent );
		when( imageMagick.getSize( anyString() ) ).thenReturn( new Size( 64, 64) );
		when( pathBuilder.createManualImagePath( (ImageVersion) anyObject(), eq(applicationId), eq(groupId),
		                                         (Date) anyObject(), (ImageSpecifier) anyObject()) ).thenReturn( "/temp" );

		ModelAndView mav = controller.uploadImage( applicationId, groupId, true, uploadModel, true );

		assertEquals( UploadStatus.SUCCES, mav.getModel().get( "status" ) );
	}

	@Test
	public void apiReplaceImageUnknownKey() throws IOException, InterruptedException
	{
		String userKey = authenticationKey;
		String imageKey = "existingImage";
		String originalFileName = "zeOriginalFilename";

		ImageUploadModel uploadModel = new ImageUploadModel();
		uploadModel.setUserKey( userKey );
		uploadModel.setOriginalFilename( originalFileName );
		uploadModel.setImageKey( imageKey );
		uploadModel.setImage( image );

		byte imageContent[] = {(byte) 0x80, (byte) 0x81};

		when( image.getBytes() ).thenReturn( imageContent );
		when( imageMagick.getSize( anyString() ) ).thenReturn( new Size( 64, 64) );
		when( pathBuilder.createManualImagePath( (ImageVersion) anyObject(), eq(applicationId), eq(groupId),
		                                         (Date) anyObject(), (ImageSpecifier) anyObject()) ).thenReturn( "/temp" );

		ModelAndView mav = controller.uploadImage( applicationId, groupId, true, uploadModel, true );

		assertEquals( UploadStatus.IMAGE_NOT_FOUND_ERROR, mav.getModel().get( "status" ) );
	}

	@Test
	public void apiReplaceImage() throws IOException, InterruptedException
	{
		String userKey = authenticationKey;
		String imageKey = "existingImage";
		long imageId = 1700;
		String originalFileName = "zeOriginalFilename";
		ServableImageData storedImage = new ServableImageData();
		storedImage.setId( imageId );
		storedImage.setApplicationId( applicationId );
		storedImage.setGroupId( groupId );

		ImageUploadModel uploadModel = new ImageUploadModel();
		uploadModel.setUserKey( userKey );
		uploadModel.setOriginalFilename( originalFileName );
		uploadModel.setImageKey( imageKey );
		uploadModel.setImage( image );

		VariantImage variant = new VariantImage();
		String dummyVariantpath = "whatever";

		when( image.getBytes() ).thenReturn( imageContent );
		when( imageMagick.getSize( anyString() ) ).thenReturn( new Size( 64, 64) );
		when( pathBuilder.createManualImagePath( (ImageVersion) anyObject(), eq(applicationId), eq(groupId),
		                                         (Date) anyObject(), (ImageSpecifier) anyObject()) ).thenReturn( "/temp" );


		when ( pathBuilder.imageIdFromRemoteId( imageKey ) ).thenReturn( imageId );
		when ( imageService.getImageById( imageId ) ).thenReturn( storedImage );
		when ( variantService.getAllVariantsForImage( imageId )).thenReturn( Arrays.asList( variant ) );
		when ( pathBuilder.generateOriginalImagePath( storedImage) ).thenReturn( tempFile.getPath() );

		when ( pathBuilder.generateVariantImagePath( (ServableImageData) anyObject(), (ImageSpecifier) anyObject()) )
				.thenReturn( dummyVariantpath );

		ModelAndView mav = controller.uploadImage( applicationId, groupId, true, uploadModel, true );

		verify( imageService ).saveImage( storedImage, true );
		verify( variantService ).getAllVariantsForImage( imageId );
		verify( variantService ).deleteVariantImage( variant );
		verify( tempFileService ).deleteFile( dummyVariantpath );
		verify( tempFileService ).deleteFile( tempFile.getPath() );

		assertEquals( UploadStatus.SUCCES, mav.getModel().get( "status" ) );
	}

	@Test
	public void apiDeleteImage()
	{
		String imageKey = "existingImage";

		ImageUploadModel uploadModel = new ImageUploadModel();
		uploadModel.setUserKey( authenticationKey );
		uploadModel.setImageKey( imageKey );

		long imageId = 1700;

		ServableImageData storedImage = new ServableImageData();
		storedImage.setId( imageId );
		storedImage.setApplicationId( applicationId );
		storedImage.setGroupId( groupId );

		when ( pathBuilder.imageIdFromRemoteId( imageKey ) ).thenReturn( imageId );

		when ( imageService.getImageById( imageId ) ).thenReturn( storedImage );

		controller.deleteImage( applicationId, groupId, true, uploadModel, true );

		verify( imageStoreService, times(1) ).deleteImage( storedImage );
	}
}
