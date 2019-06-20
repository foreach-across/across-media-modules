package com.foreach.imageserver.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
public class TestColorDto
{
	@Test
	public void namedColor() {
		assertThat( ColorDto.from( "white" ).getValue() ).isEqualTo( "white" );
		assertThat( ColorDto.from( "white" ).toString() ).isEqualTo( "white" );
	}

	@Test
	public void hexColor() {
		assertThat( ColorDto.from( "hex:aabbff" ).getValue() ).isEqualTo( "#aabbff" );
		assertThat( ColorDto.from( "hex:aabbff" ).toString() ).isEqualTo( "hex:aabbff" );
		assertThat( ColorDto.from( "#e32e2e" ).getValue() ).isEqualTo( "#e32e2e" );
		assertThat( ColorDto.from( "#e32e2e" ).toString() ).isEqualTo( "hex:e32e2e" );
	}

	@Test
	public void rgbMethod() {
		assertThat( ColorDto.from( "rgb(150,150,150,10)" ).getValue() ).isEqualTo( "rgb(150,150,150,10)" );
		assertThat( ColorDto.from( "rgb(150,150,150,10)" ).toString() ).isEqualTo( "rgb(150,150,150,10)" );
	}

	@Test
	public void equals() {
		assertThat( ColorDto.from( "hex:123456" ) ).isEqualTo( ColorDto.from( "#123456" ) );
	}
}
