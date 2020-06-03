package test.repositories;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageProfile;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.repositories.ImageProfileRepository;
import com.foreach.imageserver.core.repositories.ImageRepository;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test.AbstractIntegrationTest;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImageRepositoryTest extends AbstractIntegrationTest
{

	@Autowired
	private ImageRepository imageRepository;
	@Autowired
	private ImageProfileRepository imageProfileRepository;

	@Test
	public void insertAndGetById() {
		ImageProfile imageProfile = new ImageProfile();
		imageProfile.setName( "dummy_profile" );
		imageProfileRepository.save( imageProfile );

		Image writtenImage = new Image();
		writtenImage.setExternalId( "external_id" );
		writtenImage.setDateCreated( DateUtils.truncate( new Date(), Calendar.SECOND ) );
		writtenImage.setDimensions( dimensions( 111, 222 ) );
		writtenImage.setImageType( ImageType.EPS );
		writtenImage.setImageProfileId( imageProfile.getId() );
		imageRepository.create( writtenImage );

		Image readImage = imageRepository.findById( writtenImage.getId() ).orElse(null);
		assertEquals( writtenImage.getId(), readImage.getId() );
		assertEquals( writtenImage.getExternalId(), readImage.getExternalId() );
		assertTrue( DateUtils.truncatedEquals( writtenImage.getDateCreated(), readImage.getDateCreated(),
		                                       Calendar.SECOND ) );
		assertEquals( writtenImage.getDimensions().getWidth(), readImage.getDimensions().getWidth() );
		assertEquals( writtenImage.getDimensions().getHeight(), readImage.getDimensions().getHeight() );
		assertEquals( writtenImage.getImageType(), readImage.getImageType() );
	}

	@Test
	public void insertAndGetByExternalId() {
		ImageProfile imageProfile = new ImageProfile();
		imageProfile.setName( "dummy_profile 2" );
		imageProfileRepository.save( imageProfile );

		Image writtenImage = new Image();
		writtenImage.setExternalId( "external_id2" );

		Calendar cal = Calendar.getInstance();
		cal.set( 2011, Calendar.FEBRUARY, 28 );
		cal.set( Calendar.HOUR_OF_DAY, 9 );

		writtenImage.setDateCreated( DateUtils.truncate( cal.getTime(), Calendar.SECOND ) );
		writtenImage.setDimensions( dimensions( 111, 222 ) );
		writtenImage.setImageType( ImageType.EPS );
		writtenImage.setImageProfileId( imageProfile.getId() );
		writtenImage.setFileSize( Long.MAX_VALUE );
		imageRepository.create( writtenImage );

		Image readImage = imageRepository.getByExternalId( writtenImage.getExternalId() );
		assertEquals( writtenImage.getId(), readImage.getId() );
		assertEquals( writtenImage.getExternalId(), readImage.getExternalId() );
		assertTrue( DateUtils.truncatedEquals( writtenImage.getDateCreated(), readImage.getDateCreated(),
		                                       Calendar.SECOND ) );
		assertEquals( writtenImage.getDimensions().getWidth(), readImage.getDimensions().getWidth() );
		assertEquals( "2011/02/28/09", readImage.getVariantPath() );
		assertEquals( "2011/02/28/09", readImage.getOriginalPath() );
		assertEquals( writtenImage.getDimensions().getHeight(), readImage.getDimensions().getHeight() );
		assertEquals( writtenImage.getImageType(), readImage.getImageType() );
		assertEquals( Long.MAX_VALUE, readImage.getFileSize() );

		imageRepository.update( readImage );
		Image dontChangePathsOnImage = imageRepository.getByExternalId( readImage.getExternalId() );
		assertEquals( "2011/02/28/09", dontChangePathsOnImage.getVariantPath() );
		assertEquals( "2011/02/28/09", dontChangePathsOnImage.getOriginalPath() );

	}

	private Dimensions dimensions( int width, int height ) {
		Dimensions dimensions = new Dimensions();
		dimensions.setWidth( width );
		dimensions.setHeight( height );
		return dimensions;
	}

}
