package com.foreach.imageserver.dto;

import lombok.*;

/**
 * Represents either a named color or some other form of color specification.
 * Supports hex representations as either <code>#ffffff</code> or <code>hex:ffffff</code> for the input
 * (using {@link #from(String)}, but will always write as {@code hex:ffffff} when using {@link #toString()}.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ColorDto
{
	public static final ColorDto TRANSPARENT = ColorDto.from( "transparent" );
	public static final ColorDto WHITE = ColorDto.from( "#ffffff" );
	public static final ColorDto BLACK = ColorDto.from( "#000000" );

	@Getter
	private final String value;

	@Override
	public String toString() {
		return value.startsWith( "#" ) ? "hex:" + value.substring( 1 ) : value;
	}

	public static ColorDto from( @NonNull String value ) {
		String lowered = value.toLowerCase();
		if ( lowered.startsWith( "hex:" ) ) {
			lowered = "#" + lowered.substring( 4 );
		}
		return new ColorDto( lowered );
	}
}
