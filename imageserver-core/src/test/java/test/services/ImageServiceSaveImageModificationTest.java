package test.services;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.managers.ImageModificationManager;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.services.ImageServiceImpl;
import com.foreach.imageserver.core.services.ImageStoreService;
import com.foreach.imageserver.core.services.exceptions.CropOutsideOfImageBoundsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.stream.Stream;

public class ImageServiceSaveImageModificationTest
{
	@Mock(name = "imageModificationManager")
	private ImageModificationManager imageModificationManager;

	@Mock(name = "imageStoreService")
	private ImageStoreService imageStoreService;

	@InjectMocks
	private ImageService imageService = new ImageServiceImpl();

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.initMocks( this );
	}

	public static Stream<Arguments> data() {
		return Stream.of(
				Arguments.of( 0, 0, 50, 100, 50, 100, false, "full size" ),
				Arguments.of( 10, 10, 30, 60, 50, 100, false, "small inside" ),
				Arguments.of( -10, 0, 50, 100, 50, 100, true, "outside left" ),
				Arguments.of( 10, 0, 50, 100, 50, 100, true, "outside right" ),
				Arguments.of( 0, -10, 50, 100, 50, 100, true, "outside top" ),
				Arguments.of( 0, 10, 50, 100, 50, 100, true, "outside bottom" ),
				Arguments.of( 0, 0, 100, 200, 50, 100, true, "outside right bottom" )
		);
	}

	@ParameterizedTest(name = "{index} - {7}:  crop({0},{1},{2},{3}) image({4},{5}))")
	@MethodSource("data")
	public void saveImageModification( int cropX,
	                                   int cropY,
	                                   int cropWidth,
	                                   int cropHeight,
	                                   int imageWidth,
	                                   int imageHeight,
	                                   boolean throwsError,
	                                   String situation ) throws Exception {
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

	@ParameterizedTest(name = "{index} - {7}:  crop({0},{1},{2},{3}) image({4},{5}))")
	@MethodSource("data")
	public void saveImageModifications( int cropX,
	                                    int cropY,
	                                    int cropWidth,
	                                    int cropHeight,
	                                    int imageWidth,
	                                    int imageHeight,
	                                    boolean throwsError,
	                                    String situation ) throws Exception {
		try {
			imageService.saveImageModifications( Arrays.asList( createModification( cropX, cropY, cropWidth, cropHeight ) ),
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
