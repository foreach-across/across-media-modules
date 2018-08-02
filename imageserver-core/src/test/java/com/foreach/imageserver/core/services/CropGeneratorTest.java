package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.managers.ImageModificationManager;
import com.foreach.imageserver.core.managers.ImageProfileManager;
import com.foreach.imageserver.core.managers.ImageResolutionManager;
import com.foreach.imageserver.dto.CropDto;
import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 */
@RunWith(MockitoJUnitRunner.class)
public class CropGeneratorTest
{
	private int resolutionIdCounter;

	private CropGeneratorImpl cropGenerator;

	private ImageResolutionServiceImpl imageResolutionService;

	private Image image;
	private ImageContext context;
	private ImageModificationDto modification;

	@Mock
	private ImageModificationManager imageModificationManager;

	@Mock
	private ImageProfileManager imageProfileManager;

	@Mock
	private ImageResolutionManager imageResolutionManager;

	@Before
	public void before() {
		imageResolutionService = new ImageResolutionServiceImpl();
		cropGenerator = new CropGeneratorImpl();
		cropGenerator.setCropGeneratorUtil( new CropGeneratorUtilImpl() );
		cropGenerator.setImageModificationManager( imageModificationManager );
		cropGenerator.setImageProfileManager( imageProfileManager );
		cropGenerator.setImageResolutionManager( imageResolutionManager );
		cropGenerator.setImageResolutionService( imageResolutionService );

		imageResolutionService.setImageResolutionManager( imageResolutionManager );

		image = new Image();
		image.setId( 1 );
		image.setDimensions( new Dimensions( 4000, 2000 ) );

		context = new ImageContext();
		context.setId( 1L );

		modification = null;
		resolutionIdCounter = 1;
	}

	@Test
	public void scaledVersionOfOriginal() {
		requestModification( 2000, 1000 );
		assertResolution( 2000, 1000 );
		assertCrop( 0, 0, 4000, 2000 );

		requestModification( 1000, 500 );
		assertResolution( 1000, 500 );
		assertCrop( 0, 0, 4000, 2000 );

		requestModification( 2000, 0 );
		assertResolution( 2000, 1000 );
		assertCrop( 0, 0, 4000, 2000 );

		requestModification( 0, 500 );
		assertResolution( 1000, 500 );
		assertCrop( 0, 0, 4000, 2000 );

		requestModification( 8000, 4000 );
		assertResolution( 8000, 4000 );
		assertCrop( 0, 0, 4000, 2000 );
	}

	@Test
	public void defaultCropsUseImageCenter() {
		requestModification( 4000, 1000 );
		assertResolution( 4000, 1000 );
		assertCrop( 0, 500, 4000, 1000 );

		requestModification( 2000, 2000 );
		assertResolution( 2000, 2000 );
		assertCrop( 1000, 0, 2000, 2000 );
	}

	@Test
	public void useSpecificModificationIfPresent() {
		ImageModification expectedModification = new ImageModification();
		expectedModification.setCrop( new Crop( 1, 2, 3, 4 ) );

		when( imageModificationManager.getById( 1, 1, 1 ) ).thenReturn( expectedModification );

		requestModification( 2000, 500 );
		assertResolution( 2000, 500 );
		assertCrop( 1, 2, 3, 4 );
	}

	@Test
	public void useRegisteredModificationFromHigherResolutionWithSameAspectRatio() {
		// register a crop for 2000x2000 and 500x500
		List<ImageModification> registeredModifications = Arrays.asList(
				createModification( 500, 500, 5, 6, 7, 8 ),
				createModification( 3000, 1000, 0, 1000, 3000, 1000 ),
				createModification( 2000, 2000, 1, 2, 3, 4 )
		);

		when( imageModificationManager.getAllModifications( 1 ) ).thenReturn( registeredModifications );

		// 1000x1000 request should use the 2000x2000 crop
		requestModification( 1000, 1000 );
		assertResolution( 1000, 1000 );
		assertCrop( 1, 2, 3, 4 );

		// 250x250 request should use the 500x500 crop
		requestModification( 250, 250 );
		assertResolution( 250, 250 );
		assertCrop( 5, 6, 7, 8 );
	}

	@Test
	public void inCaseOfSameDistanceTheHigherResolutionShouldBeUsed() {
		List<ImageModification> registeredModifications = Arrays.asList(
				createModification( 500, 500, 3500, 1500, 500, 500 ),
				createModification( 3000, 1000, 0, 1000, 3000, 1000 ),
				createModification( 1500, 1500, 1, 2, 3, 4 )
		);

		when( imageModificationManager.getAllModifications( 1 ) ).thenReturn( registeredModifications );

		// 1000x1000 request should use the 1500x1500 crop
		requestModification( 1000, 1000 );
		assertResolution( 1000, 1000 );
		assertCrop( 1, 2, 3, 4 );
	}

	@Test
	public void inCaseOfOnlyLowerResolutionCropAvailableThatOneShouldBeUsed() {
		List<ImageModification> registeredModifications = Arrays.asList(
				createModification( 800, 800, 3500, 1500, 500, 500 ),
				createModification( 3000, 1000, 0, 1000, 3000, 1000 ),
				createModification( 1500, 1500, 1, 2, 3, 4 )
		);

		when( imageModificationManager.getAllModifications( 1 ) ).thenReturn( registeredModifications );

		requestModification( 1600, 1600 );
		assertResolution( 1600, 1600 );
		assertCrop( 1, 2, 3, 4 );

		requestModification( 10000, 10000 );
		assertResolution( 10000, 10000 );
		assertCrop( 1, 2, 3, 4 );
	}

	@Test
	public void inCaseOfOriginalAspectRatioOtherCropsAreIgnored() {
		List<ImageModification> registeredModifications = Arrays.asList(
				createModification( 800, 800, 3500, 1500, 500, 500 ),
				createModification( 3000, 1000, 0, 1000, 3000, 1000 ),
				createModification( 1500, 1500, 0, 0, 2000, 2000 )
		);

		when( imageModificationManager.getAllModifications( 1 ) ).thenReturn( registeredModifications );

		requestModification( 2000, 1000 );
		assertResolution( 2000, 1000 );
		assertCrop( 0, 0, 4000, 2000 );
	}

	@Test
	public void inCaseOfBigDistanceDifferenceTheClosestResolutionShouldBeUsed() {
		List<ImageModification> registeredModifications = Arrays.asList(
				createModification( 800, 800, 3500, 1500, 500, 500 ),
				createModification( 3000, 1000, 0, 1000, 3000, 1000 ),
				createModification( 1500, 1500, 1, 2, 3, 4 )
		);

		when( imageModificationManager.getAllModifications( 1 ) ).thenReturn( registeredModifications );
		// TODO: I would prefer to always use the highest available best matching crop.
		// 1000x1000 request should use the 800x800 crop
		requestModification( 1000, 1000 );
		assertResolution( 1000, 1000 );
		assertCrop( 1, 2, 3, 4 );
	}

	private void assertCrop( int x, int y, int width, int height ) {
		CropDto crop = new CropDto( x, y, width, height );
		assertEquals( crop, modification.getCrop() );
	}

	private void assertResolution( int width, int height ) {
		assertEquals( new ImageResolutionDto( width, height ), modification.getResolution() );
	}

	private void requestModification( int width, int height ) {
		ImageResolution resolution = new ImageResolution();
		resolution.setId( 1 );
		resolution.setWidth( width );
		resolution.setHeight( height );

		this.modification = cropGenerator.buildModificationDto( image, context, resolution );
	}

	private ImageModification createModification( int width, int height, int x, int y, int cropWidth, int cropHeight ) {
		ImageModification modification = new ImageModification();
		ImageResolution resolution = new ImageResolution();
		resolution.setId( resolutionIdCounter++ );
		resolution.setWidth( width );
		resolution.setHeight( height );

		when( imageResolutionManager.getById( resolution.getId() ) ).thenReturn( resolution );

		modification.setContextId( 1 );
		modification.setCrop( new Crop( x, y, cropWidth, cropHeight ) );
		modification.setImageId( 1 );
		modification.setResolutionId( resolution.getId() );

		return modification;
	}

}
