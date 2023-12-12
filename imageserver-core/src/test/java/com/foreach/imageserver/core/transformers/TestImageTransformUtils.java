package com.foreach.imageserver.core.transformers;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.dto.CropDto;
import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageTransformDto;
import com.foreach.imageserver.math.AspectRatio;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static com.foreach.imageserver.dto.ImageTransformDto.builder;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
public class TestImageTransformUtils
{
	private ImageTransformUtils utils = new ImageTransformUtils();

	private ImageAttributes attributes = ImageAttributes.builder()
	                                                    .type( ImageType.PNG )
	                                                    .dimensions( new Dimensions( 1600, 1200 ) )
	                                                    .sceneCount( 1 )
	                                                    .build();

	@Test
	public void emptyTransformRemainsEmpty() {
		ImageTransformDto normalized = utils.normalize( builder().build(), attributes );
		assertThat( normalized.isEmpty() ).isTrue();
	}

	@Test
	public void explicitlySettingOriginalWidthAndHeightCounts() {
		assertNormalized( t().width( 1600 ).height( 1200 ) ).isEqualTo( t().width( 1600 ).height( 1200 ) );
	}

	@Test
	public void missingDimensionIsCalculatedAccordingToOriginalDimensions() {
		assertNormalized( t().width( 800 ) ).isEqualTo( t().width( 800 ).height( 600 ) );
		assertNormalized( t().width( 800 ).height( -1 ) ).isEqualTo( t().width( 800 ).height( 600 ) );
		assertNormalized( t().width( 800 ).height( 0 ) ).isEqualTo( t().width( 800 ).height( 600 ) );

		assertNormalized( t().height( 600 ) ).isEqualTo( t().width( 800 ).height( 600 ) );
		assertNormalized( t().height( 600 ).width( -1 ) ).isEqualTo( t().width( 800 ).height( 600 ) );
		assertNormalized( t().height( 600 ).width( 0 ) ).isEqualTo( t().width( 800 ).height( 600 ) );
	}

	@Test
	public void cropNormalizationIsApplied() {
		assertNormalized( t().crop( CropDto.builder().width( 450 ).height( 300 ).build() ) )
				.isEqualTo( t().crop( CropDto.builder().width( 450 ).height( 300 ).build() ) );
		assertNormalized( t().crop( CropDto.builder().width( 450 ).height( 300 ).box( new DimensionsDto( 450, 300 ) ).build() ) )
				.isEqualTo( t().crop( CropDto.builder().width( 1600 ).height( 1200 ).build() ) );
	}

	@Test
	public void maxDimensionsAreCalculatedIfNecessaryAndUsedToDownscaleTheOutput() {
		assertNormalized( t().maxWidth( 640 ).maxHeight( 480 ) ).isEqualTo( t().width( 640 ).height( 480 ) );
		assertNormalized( t().width( 800 ).height( 600 ).maxWidth( 640 ).maxHeight( 480 ) ).isEqualTo( t().width( 640 ).height( 480 ) );

		assertNormalized( t().maxWidth( 800 ) ).isEqualTo( t().width( 800 ).height( 600 ) );
		assertNormalized( t().maxHeight( 600 ) ).isEqualTo( t().width( 800 ).height( 600 ) );
	}

	@Test
	public void largestCenterCropIsAssignedIfOutputDimensionsHaveDifferentAspectRatioFromOriginal() {
		assertNormalized( t().width( 1500 ).height( 1100 ) )
				.isEqualTo( t().crop( CropDto.builder().x( 0 ).y( 13 ).width( 1600 ).height( 1173 ).build() ).width( 1500 ).height( 1100 ) );
		assertNormalized( t().width( 750 ).height( 550 ) )
				.isEqualTo( t().crop( CropDto.builder().x( 0 ).y( 13 ).width( 1600 ).height( 1173 ).build() ).width( 750 ).height( 550 ) );

		assertNormalized( t().width( 123 ).height( 456 ) )
				.isEqualTo( t().crop( CropDto.builder().x( 638 ).y( 0 ).width( 324 ).height( 1200 ).build() ).width( 123 ).height( 456 ) );
	}

	@Test
	public void requestedAspectRatioIsUsedForCalculatingDimensions() {
		assertNormalized( t().width( 123 ).height( 456 ).aspectRatio( new AspectRatio( "16/9" ) ) )
				.isEqualTo( t().crop( CropDto.builder().x( 638 ).y( 0 ).width( 324 ).height( 1200 ).build() ).width( 123 ).height( 456 ) );

		assertNormalized( t().width( 1280 ).aspectRatio( new AspectRatio( "16/9" ) ) )
				.isEqualTo( t().crop( CropDto.builder().x( 0 ).y( 150 ).width( 1600 ).height( 900 ).build() ).width( 1280 ).height( 720 ) );
		assertNormalized( t().height( 720 ).aspectRatio( new AspectRatio( "16/9" ) ) )
				.isEqualTo( t().crop( CropDto.builder().x( 0 ).y( 150 ).width( 1600 ).height( 900 ).build() ).width( 1280 ).height( 720 ) );

		// original corresponds exactly, don't force width/height
		assertNormalized( t().aspectRatio( new AspectRatio( "4/3" ) ) ).isEqualTo( t() );

		// use original dimensions for conversion
		assertNormalized( t().aspectRatio( new AspectRatio( "16/9" ) ) )
				.isEqualTo( t().crop( CropDto.builder().x( 0 ).y( 150 ).width( 1600 ).height( 900 ).build() ).width( 1600 ).height( 900 ) );
		assertNormalized( t().aspectRatio( new AspectRatio( "3/4" ) ) )
				.isEqualTo( t().crop( CropDto.builder().x( 350 ).y( 0 ).width( 900 ).height( 1200 ).build() ).width( 900 ).height( 1200 ) );

		// max dimensions also respect aspect ratio
		assertNormalized( t().maxWidth( 2000 ).aspectRatio( new AspectRatio( "16/9" ) ) )
				.isEqualTo( t().crop( CropDto.builder().x( 0 ).y( 150 ).width( 1600 ).height( 900 ).build() ).width( 1600 ).height( 900 ) );
		assertNormalized( t().maxWidth( 1280 ).aspectRatio( new AspectRatio( "16/9" ) ) )
				.isEqualTo( t().crop( CropDto.builder().x( 0 ).y( 150 ).width( 1600 ).height( 900 ).build() ).width( 1280 ).height( 720 ) );
		assertNormalized( t().maxHeight( 720 ).aspectRatio( new AspectRatio( "16/9" ) ) )
				.isEqualTo( t().crop( CropDto.builder().x( 0 ).y( 150 ).width( 1600 ).height( 900 ).build() ).width( 1280 ).height( 720 ) );
	}

	private ImageTransformDto.ImageTransformDtoBuilder t() {
		return builder();
	}

	private NormalizedTransformAssert assertNormalized( ImageTransformDto.ImageTransformDtoBuilder original ) {
		return new NormalizedTransformAssert( utils.normalize( original.build(), attributes ) );
	}

	@RequiredArgsConstructor
	private class NormalizedTransformAssert
	{
		private final ImageTransformDto actual;

		void isEqualTo( ImageTransformDto.ImageTransformDtoBuilder expected ) {
			assertThat( actual ).isEqualTo( expected.build() );
		}
	}
}

