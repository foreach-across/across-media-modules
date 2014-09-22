package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.managers.ImageModificationManager;
import com.foreach.imageserver.core.services.exceptions.CropOutsideOfImageBoundsException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class ImageServiceSaveImageModificationTest
{

	private int cropX;
	private int cropY;
	private int cropWidth;
	private int cropHeight;
	private int imageWidth;
	private int imageHeight;
	private boolean throwsError;
	private String situation;

	@Mock(name = "imageModificationManager")
	private ImageModificationManager imageModificationManager;

	@Mock(name = "imageStoreService")
	private ImageStoreService imageStoreService;

	@InjectMocks
	private ImageService imageService = new ImageServiceImpl();

	public ImageServiceSaveImageModificationTest( int cropX,
	                                              int cropY,
	                                              int cropWidth,
	                                              int cropHeight,
	                                              int imageWidth,
	                                              int imageHeight,
	                                              boolean throwsError,
	                                              String situation ) {
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.cropWidth = cropWidth;
		this.cropHeight = cropHeight;
		this.cropX = cropX;
		this.cropY = cropY;
		this.throwsError = throwsError;
		this.situation = situation;
	}

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks( this );
	}

	@Parameterized.Parameters(name = "{index} - {7}:  crop({0},{1},{2},{3}) image({4},{5}))")
	public static Collection<Object[]> data() {
		List<Object[]> data = new ArrayList<Object[]>();

		data.add( new Object[] { 0, 0, 50, 100, 50, 100, false, "full size" } );
		data.add( new Object[] { 10, 10, 30, 60, 50, 100, false, "small inside" } );
		data.add( new Object[] { -10, 0, 50, 100, 50, 100, true, "outside left" } );
		data.add( new Object[] { 10, 0, 50, 100, 50, 100, true, "outside right" } );
		data.add( new Object[] { 0, -10, 50, 100, 50, 100, true, "outside top" } );
		data.add( new Object[] { 0, 10, 50, 100, 50, 100, true, "outside bottom" } );
		data.add( new Object[] { 0, 0, 100, 200, 50, 100, true, "outside right bottom" } );

		return data;
	}

	@Test
	public void saveImageModification() throws Exception {
		try {
			imageService.saveImageModification( createModification( cropX, cropY, cropWidth, cropHeight ),
			                                    createImage( imageWidth, imageHeight ) );
		}
		catch ( CropOutsideOfImageBoundsException e ) {
			if ( !throwsError ) {
				throw new Exception( "Should not have thrown exception!" );
			}
			return;
		}
		if ( throwsError ) {
			throw new Exception( "Should have thrown exception!" );
		}
	}

	@Test
	public void saveImageModifications() throws Exception {
		try {
			imageService.saveImageModifications( Arrays.asList(createModification( cropX, cropY, cropWidth, cropHeight )),
			                                    createImage( imageWidth, imageHeight ) );
		}
		catch ( CropOutsideOfImageBoundsException e ) {
			if ( !throwsError ) {
				throw new Exception( "Should not have thrown exception!" );
			}
			return;
		}
		if ( throwsError ) {
			throw new Exception( "Should have thrown exception!" );
		}
	}

	private Image createImage( int width, int height ) {
		Image image = new Image();
		image.setDimensions( new Dimensions( width, height ) );
		return image;
	}

	private ImageModification createModification( int x, int y, int width, int height ) {
		ImageModification modification = new ImageModification();
		modification.setCrop( createCrop( x, y, width, height ) );
		return modification;
	}

	private Crop createCrop( int x, int y, int width, int height ) {
		Crop crop = new Crop();
		crop.setX( x );
		crop.setY( y );
		crop.setWidth( width );
		crop.setHeight( height );
		return crop;
	}
}
