package com.foreach.imageserver.core.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.foreach.imageserver.core.config.ServicesConfiguration.ORIGINALS_REPOSITORY;

public class DefaultImageFileDescriptorFactoryTest
{
	private DefaultImageFileDescriptorFactory defaultImageFileDescriptorFactory;
	private Image image;
	private Image variantImage;
	private String customRepository;

	private String originalPath = "/appl/shared/imageserver_img/originals/2018/12/11/09/";
	private String variantPath = "/appl/shared/imageserver_img/variants/ONLINE/2018/12/11/09/";

	@Before
	public void setup() {
		defaultImageFileDescriptorFactory = new DefaultImageFileDescriptorFactory();

		image = new Image();
		image.setId( 650071L);
		image.setExternalId( "650071" );
		image.setOriginalPath( originalPath );
		image.setTemporaryImage( false );
		image.setImageType( ImageType.PNG );

		variantImage = new Image();
		variantImage.setId( 650071L );
		variantImage.setExternalId( "650071-w300-h200" );
		variantImage.setVariantPath( variantPath );
		variantImage.setTemporaryImage( false );
		variantImage.setImageType( ImageType.JPEG );

		customRepository = "s3-files";
	}

	@Test
	public void oldImageOriginalPathGivesRightFileDescriptor() {
		FileDescriptor fileDescriptor = defaultImageFileDescriptorFactory.createForOriginal( image );

		Assert.assertEquals( "originals", fileDescriptor.getRepositoryId() );
		Assert.assertEquals( "/appl/shared/imageserver_img/originals/2018/12/11/09/", fileDescriptor.getFolderId() );
		Assert.assertEquals( "originals:/appl/shared/imageserver_img/originals/2018/12/11/09/:650071.png", fileDescriptor.getUri() );
		Assert.assertEquals( "650071.png", fileDescriptor.getFileId() );
	}

	@Test
	public void newImageOriginalPathGivesRightFileDescriptor() {
		image.setOriginalPath( customRepository + ":" + originalPath );
		FileDescriptor fileDescriptor = defaultImageFileDescriptorFactory.createForOriginal( image );

		Assert.assertEquals( "s3-files", fileDescriptor.getRepositoryId() );
		Assert.assertEquals( "/appl/shared/imageserver_img/originals/2018/12/11/09/", fileDescriptor.getFolderId() );
		Assert.assertEquals( "s3-files:/appl/shared/imageserver_img/originals/2018/12/11/09/:650071.png", fileDescriptor.getUri() );
		Assert.assertEquals( "650071.png", fileDescriptor.getFileId() );
	}

}
