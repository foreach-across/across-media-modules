package com.foreach.imageserver.dto;

import com.foreach.imageserver.math.AspectRatio;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a single transformation that can be done on an image.
 * A transform can be written to and converted from a single line string which can easily be used in URLs.
 * A transform string has the form of {@code PARAM_VALUE,PARAM_VALUE}
 * <p/>
 * The following parameters are supported:
 * <ul>
 * <li><strong>scene</strong>: {@link #setScene(Integer)}</li>
 * <li><strong>w</strong>: {@link #setWidth(Integer)}</li>
 * <li><strong>h</strong>: {@link #setHeight(Integer)}</li>
 * <li><strong>ar</strong>: {@link #setAspectRatio(AspectRatio)}</li>
 * <li><strong>maxw</strong>: {@link #setMaxWidth(Integer)} </li>
 * <li><strong>maxh</strong>: {@link #setMaxHeight(Integer)} </li>
 * <li><strong>dpi</strong>: {@link #setDpi(Integer)}</li>
 * <li><strong>cx</strong>: set crop x coordinate</li>
 * <li><strong>cy</strong>: set crop y coordinate</li>
 * <li><strong>cw</strong>: set crop width</li>
 * <li><strong>ch</strong>: set crop height</li>
 * <li><strong>csw</strong>: set the assumed width of the source image for the crop coordinates</li>
 * <li><strong>csh</strong>: set the assumed height of the source image for the crop coordinates</li>
 * <li><strong>cbw</strong>: set the width of the box that contained the source image for the crop coordinates</li>
 * <li><strong>cbh</strong>: set the height of the box that contained the source image for the crop coordinates</li>
 * <li><strong>color</strong>: {@link #setColorSpace(ColorSpaceDto)}</li>
 * <li><strong>bg</strong>: {@link #setBackgroundColor(ColorDto)}</li>
 * <li><strong>alpha</strong>: {@link #setAlphaColor(ColorDto)}</li>
 * <li><strong>q</strong>: {@link #setQuality(Integer)}</li>
 * <li><strong>o</strong>: output type of the transform result, extension of supported {@link ImageTypeDto}, see {@link #setOutputType(ImageTypeDto)}</li>
 * </ul>
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageTransformDto
{
	/**
	 * The specific scene of the source image that should be transformed.
	 * Only relevant for multi-scene formats like PDF where a scene corresponds to a page in the PDF.
	 * If not specified, the output type will determine if either the entire original image or a specific scene will be used.
	 */
	private Integer scene;

	/**
	 * The width in pixels of the resulting image.
	 * If both width and height are not specified, the original image width will be assumed.
	 * If height is specified but width is not, a width will be calculated according to the aspect ratio of the original image.
	 * Any value smaller than 1 will behave as if width is not specified.
	 */
	private Integer width;

	/**
	 * The height in pixels of the resulting image.
	 * If both width and height are not specified, the original image height will be assumed.
	 * If width is specified but height is not, a height will be calculated according to the aspect ratio of the original image.
	 * Any value smaller than 1 will behave as if height is not specified.
	 */
	private Integer height;

	/**
	 * Maximum width of the resulting image.
	 * Regardless of {@link #width}, the resulting image will be downscaled to fit in the boundaries box
	 * defined by maximum width and height (when specified).
	 * Can be used to scale an image to fit inside this box without having to specify width or height.
	 */
	private Integer maxWidth;

	/**
	 * Maximum height of the resulting image.
	 * Regardless of {@link #height}, the resulting image will be downscaled to fit in the boundaries box
	 * defined by maximum width and height (when specified).
	 * Can be used to scale an image to fit inside this box without having to specify width or height.
	 */
	private Integer maxHeight;

	/**
	 * The aspect ratio that the resulting image should have.
	 * If both width and height are specified, this parameter will be ignored.
	 * If either width or height is specified, the aspect ratio will be used to calculate the other dimension accordingly.
	 * If neither width and height is specified, the actual width and height will be calculated by looking at the original image
	 * dimensions and using them to get the largest possible values matching the requested aspect ratio.
	 */
	private AspectRatio aspectRatio;

	/**
	 * DPI that should be used for processing the image (and in the resulting output image).
	 * A higher DPI will result in larger output files but with much more detail.
	 * Equivalent of {@link ImageModificationDto#getDensity()} which will most likely be deprecated in the future.
	 */
	private Integer dpi;

	/**
	 * Contains the settings for the crop that should be taken from the original image.
	 * If not specified, no cropping will be applied and the entire original image will be used as the base.
	 */
	private CropDto crop;

	/**
	 * Quality for the created image, relevant for lossy image formats like JPEG.
	 * Should be percentage represented as a value between {@code 1} and {@code 100}.
	 * If not specified, the default quality configured on the application will be used.
	 */
	private Integer quality;

	/**
	 * Color specification for the background of the image.
	 * Any alpha channel will be changed to this color. If you want to replace an existing color instead, you can
	 * combine with {@link #setAlphaColor(ColorDto)}.
	 */
	private ColorDto backgroundColor;

	/**
	 * Color specification of a color that should be made transparent.  Only hex value without the leading {@code #} is supported.
	 */
	private ColorDto alphaColor;

	/**
	 * Color space for the resulting image, can for example be used to convert to grayscale.
	 */
	private ColorSpaceDto colorSpace;

	/**
	 * The output type for this transform.
	 */
	private ImageTypeDto outputType;

	/**
	 * Parse a string into the transform it represents.
	 *
	 * @param transformString version
	 * @return transform
	 */
	public static ImageTransformDto from( @NonNull String transformString ) {
		ImageTransformDto dto = new ImageTransformDto();

		CropDto crop = new CropDto();

		Stream.of( transformString.split( "," ) )
		      .map( StringParam::parse )
		      .forEach( param -> {
			      if ( "w".equals( param.key ) ) {
				      dto.width = param.intValue();
			      }
			      else if ( "h".equals( param.key ) ) {
				      dto.height = param.intValue();
			      }
			      else if ( "scene".equals( param.key ) ) {
				      dto.scene = param.intValue();
			      }
			      else if ( "maxw".equals( param.key ) ) {
				      dto.maxWidth = param.intValue();
			      }
			      else if ( "maxh".equals( param.key ) ) {
				      dto.maxHeight = param.intValue();
			      }
			      else if ( "dpi".equals( param.key ) ) {
				      dto.dpi = param.intValue();
			      }
			      else if ( "cx".equals( param.key ) ) {
				      crop.setX( param.intValue() );
			      }
			      else if ( "cy".equals( param.key ) ) {
				      crop.setY( param.intValue() );
			      }
			      else if ( "cw".equals( param.key ) ) {
				      crop.setWidth( param.intValue() );
			      }
			      else if ( "ch".equals( param.key ) ) {
				      crop.setHeight( param.intValue() );
			      }
			      else if ( "csw".equals( param.key ) ) {
				      crop.getSource().setWidth( param.intValue() );
			      }
			      else if ( "csh".equals( param.key ) ) {
				      crop.getSource().setHeight( param.intValue() );
			      }
			      else if ( "cbw".equals( param.key ) ) {
				      crop.getBox().setWidth( param.intValue() );
			      }
			      else if ( "cbh".equals( param.key ) ) {
				      crop.getBox().setHeight( param.intValue() );
			      }
			      else if ( "bg".equals( param.key ) ) {
				      dto.backgroundColor = ColorDto.from( param.stringValue() );
			      }
			      else if ( "alpha".equals( param.key ) ) {
				      dto.alphaColor = ColorDto.from( param.stringValue() );
			      }
			      else if ( "color".equals( param.key ) ) {
				      dto.colorSpace = ColorSpaceDto.valueOf( StringUtils.upperCase( param.stringValue() ) );
			      }
			      else if ( "q".equals( param.key ) ) {
				      dto.quality = param.intValue();
			      }
			      else if ( "o".equals( param.key ) ) {
				      dto.outputType = ImageTypeDto.forExtension( param.stringValue() );
			      }
			      else if ( "ar".equals( param.key ) ) {
				      dto.aspectRatio = new AspectRatio( StringUtils.replace( param.stringValue(), ":", "/" ) );
			      }
		      } );

		if ( !crop.equals( new CropDto() ) ) {
			dto.crop = crop;
		}

		return dto;
	}

	@Override
	public String toString() {
		List<StringParam> parameters = new ArrayList<>();
		parameters.add( new StringParam( "scene", scene ) );
		parameters.add( new StringParam( "w", width ) );
		parameters.add( new StringParam( "h", height ) );
		parameters.add( new StringParam( "maxw", maxWidth ) );
		parameters.add( new StringParam( "maxh", maxHeight ) );

		if ( aspectRatio != null ) {
			parameters.add( new StringParam( "ar", aspectRatio.toString().replace( "/", ":" ) ) );
		}

		if ( crop != null ) {
			parameters.add( new StringParam( "cx", crop.getX() ) );
			parameters.add( new StringParam( "cy", crop.getY() ) );
			parameters.add( new StringParam( "cw", crop.getWidth() ) );
			parameters.add( new StringParam( "ch", crop.getHeight() ) );

			if ( crop.hasSource() ) {
				parameters.add( new StringParam( "csw", crop.getSource().getWidth() ) );
				parameters.add( new StringParam( "csh", crop.getSource().getHeight() ) );
			}

			if ( crop.hasBox() ) {
				parameters.add( new StringParam( "cbw", crop.getBox().getWidth() ) );
				parameters.add( new StringParam( "cbh", crop.getBox().getHeight() ) );
			}
		}

		if ( colorSpace != null ) {
			parameters.add( new StringParam( "color", StringUtils.lowerCase( colorSpace.name() ) ) );
		}

		parameters.add( new StringParam( "bg", backgroundColor ) );
		parameters.add( new StringParam( "alpha", alphaColor ) );
		parameters.add( new StringParam( "dpi", dpi ) );
		parameters.add( new StringParam( "q", quality ) );

		if ( outputType != null ) {
			parameters.add( new StringParam( "o", outputType.getExtension() ) );
		}

		return parameters.stream()
		                 .map( StringParam::toString )
		                 .filter( Objects::nonNull )
		                 .collect( Collectors.joining( "," ) );
	}

	public boolean isEmpty() {
		return this.equals( new ImageTransformDto() );
	}

	@RequiredArgsConstructor
	private static class StringParam
	{
		private final String key;
		private final Object value;

		static StringParam parse( String s ) {
			String key = StringUtils.substringBefore( s, "_" );
			String value = StringUtils.substringAfter( s, "_" );
			return new StringParam( key, value );
		}

		int intValue() {
			return Integer.parseInt( Objects.toString( value ) );
		}

		String stringValue() {
			return Objects.toString( value );
		}

		@Override
		public String toString() {
			return value != null ? key + "_" + value : null;
		}
	}
}
