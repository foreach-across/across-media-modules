package dao;

import com.foreach.imageserver.business.Image;
import com.foreach.imageserver.business.ImageType;
import com.foreach.imageserver.business.image.Dimensions;
import com.foreach.imageserver.dao.ImageDao;
import com.foreach.shared.utils.DateUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.*;

public class TestImageDao extends AbstractDaoTest
{
	@Autowired
	private ImageDao imageDao;

	@Test
	public void getKnownImageByKey() {
		Image knownImage = imageDao.getImageByKey( "validimagekey", 9999 );

		assertNotNull( knownImage );
		assertEquals( 9999001, knownImage.getId() );
		assertEquals( 9999, knownImage.getApplicationId() );
		assertEquals( "validimagekey", knownImage.getKey() );
		assertEquals( "/2013/07/07", knownImage.getFilePath() );
		assertEquals( 123456789, knownImage.getFileSize() );
		assertEquals( new Dimensions( 1600, 1200 ), knownImage.getDimensions() );
		assertEquals( ImageType.JPEG, knownImage.getImageType() );
		assertEquals( DateUtils.parseDate( "2013-07-07 13:40:33" ), knownImage.getDateCreated() );
		assertEquals( DateUtils.parseDate( "2013-08-01 17:25:31" ), knownImage.getDateUpdated() );
	}

	@Test
	public void getInvalidImageByKey() {
		assertNull( imageDao.getImageByKey( "invalidimagekey", 9999 ) );
	}

	@Test
	public void createReadUpdateDelete() {
		Image inserted = createImage();
		assertFalse( inserted.getId() > 0 );

		imageDao.insertImage( inserted );
		assertTrue( inserted.getId() > 0 );

		Image fetched = imageDao.getImageByKey( inserted.getKey(), inserted.getApplicationId() );
		compareImages( inserted, fetched );
		assertNotNull( fetched.getDateCreated() );
		assertNull( fetched.getDateUpdated() );

		Image modified = modifyImage( fetched );
		imageDao.updateImage( modified );

		fetched = imageDao.getImageByKey( inserted.getKey(), inserted.getApplicationId() );
		compareImages( modified, fetched );
		assertNotNull( fetched.getDateUpdated() );

		imageDao.deleteImage( inserted.getKey(), inserted.getApplicationId() );
		assertNull( imageDao.getImageByKey( inserted.getKey(), inserted.getApplicationId() ) );
	}

	private Image createImage() {
		Image image = new Image();
		image.setApplicationId( 9999 );
		image.setKey( RandomStringUtils.random( 50 ) );
		image.setImageType( ImageType.JPEG );
		image.setDimensions( new Dimensions( 1280, 1024 ) );
		image.setFileSize( 2048 );
		image.setFilePath( "/2013/07/08/" );
		image.setDateCreated( new Date() );

		return image;
	}

	private Image modifyImage( Image image ) {
		image.setDimensions( new Dimensions( 300, 200 ) );
		image.setImageType( ImageType.PNG );
		image.setFileSize( 10000 );
		image.setDateUpdated( new Date() );

		return image;
	}

	private void compareImages( Image left, Image right ) {
		assertEquals( left.getId(), right.getId() );
		assertEquals( left.getApplicationId(), right.getApplicationId() );
		assertEquals( left.getKey(), right.getKey() );
		assertEquals( left.getDimensions(), right.getDimensions() );
		assertEquals( left.getFilePath(), right.getFilePath() );
		assertEquals( left.getFileSize(), right.getFileSize() );
		assertEquals( left.getImageType(), right.getImageType() );
	}
}
