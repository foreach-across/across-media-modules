package com.foreach.imageserver.dto;

import com.foreach.imageserver.math.AspectRatio;
import org.junit.Test;

import static com.foreach.imageserver.dto.ImageTransformDto.builder;
import static com.foreach.imageserver.dto.ImageTransformDto.from;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
public class TestImageTransformDto
{
	@Test
	public void blankTransform() {
		assertTransformString( builder(), "" );
	}

	@Test
	public void widthAndHeight() {
		assertTransformString( builder().width( 100 ), "w_100" );
		assertTransformString( builder().height( 50 ), "h_50" );
		assertTransformString( builder().height( 50 ).width( 100 ), "w_100,h_50" );
	}

	@Test
	public void dpi() {
		assertTransformString( builder().dpi( 300 ), "dpi_300" );
	}

	@Test
	public void crop() {
		assertTransformString( builder().crop( CropDto.builder().y( 5 ).width( 300 ).height( 400 ).build() ), "cx_0,cy_5,cw_300,ch_400" );
		assertTransformString(
				builder().crop( CropDto.builder().y( 5 ).width( 300 ).height( 400 ).source( new DimensionsDto( 800, 600 ) ).build() ),
				"cx_0,cy_5,cw_300,ch_400,csw_800,csh_600"
		);
		assertTransformString(
				builder().crop( CropDto.builder().y( 5 ).width( 300 ).height( 400 ).box( new DimensionsDto( 800, 600 ) ).build() ),
				"cx_0,cy_5,cw_300,ch_400,cbw_800,cbh_600"
		);
	}

	@Test
	public void output() {
		assertTransformString( builder().output( ImageTypeDto.JPEG ), "o_jpeg" );
	}

	@Test
	public void quality() {
		assertTransformString( builder().quality( 80 ), "q_80" );
	}

	@Test
	public void background() {
		assertTransformString( builder().background( "336699" ), "bg_336699" );
	}

	@Test
	public void colorSpace() {
		assertTransformString( builder().colorSpace( ColorSpaceDto.GRAYSCALE ), "color_grayscale" );
	}

	@Test
	public void aspectRatio() {
		assertTransformString( builder().aspectRatio( new AspectRatio( "4/3" ) ), "ar_4:3" );
	}

	@Test
	public void maximumWidthAndHeight() {
		assertTransformString( builder().maximumWidth( 100 ), "maxw_100" );
		assertTransformString( builder().maximumHeight( 50 ), "maxh_50" );
		assertTransformString( builder().maximumHeight( 50 ).maximumWidth( 100 ), "maxw_100,maxh_50" );
	}

	private void assertTransformString( ImageTransformDto.ImageTransformDtoBuilder builder, String output ) {
		ImageTransformDto transformDto = builder.build();
		assertThat( transformDto.toString() ).withFailMessage( "'%s'.toString() was '%s'", output, transformDto.toString() ).isEqualTo( output );
		assertThat( from( output ) ).withFailMessage( "parse('%s') resulted in '%s'", output, from( output ) ).isEqualTo( transformDto );
	}
}
