package com.foreach.imageserver.core.transformers.imagemagick;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.services.DtoUtil;
import com.foreach.imageserver.core.transformers.*;
import com.foreach.imageserver.dto.ColorDto;
import com.foreach.imageserver.dto.ColorSpaceDto;
import com.foreach.imageserver.dto.ImageTransformDto;
import com.foreach.imageserver.dto.ImageTypeDto;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.process.Pipe;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Optional;

/**
 * Handles image transforms using ImageMagick.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@Slf4j
public class ImageMagickTransformCommandExecutor extends AbstractOrderedImageCommandExecutor<ImageTransformCommand>
{
	private static final int MAX_DPI = 1200;
	private static final int DPI_STEP = 300;

	private static final String ALPHA_BACKGROUND = "white";
	private static final double BASE_DPI = 72d;

	/**
	 * Default DPI that should be used for scalable image formats.
	 */
	@Getter
	@Setter
	private int defaultDpi = 300;

	/**
	 * Default quality setting that should be used when doing transforms.
	 * Quality can be specified on {@link ImageTransformDto} to override this default.
	 */
	@Setter
	@Getter
	private int defaultQuality = 85;

	/**
	 * Filter that should be used when resizing images.
	 * Can be {@code null} in which case no explicit filter will be set.
	 */
	@Setter
	@Getter
	private String filter = "Box";

	/**
	 * Should the {@code thumbnail} argument be used instead of the {@code resize} argument
	 * when resizing to a smaller image? Thumbnail is faster but might result in lower quality images.
	 */
	@Setter
	@Getter
	private boolean useThumbnail = false;

	@Override
	public ImageTransformerPriority canExecute( ImageTransformCommand command ) {
		return ImageTransformerPriority.FALLBACK;
	}

	@Override
	public void execute( ImageTransformCommand command ) {
		ConvertCmd cmd = new ConvertCmd();
		IMOperation op = createIMOperation( command );

		try (InputStream is = command.getOriginalImage().getImageStream()) {
			try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
				cmd.setInputProvider( new Pipe( is, null ) );
				cmd.setOutputConsumer( new Pipe( null, os ) );

				LOG.debug( "Executing IMOperation: {}", op.toString() );

				cmd.run( op );

				byte[] bytes = os.toByteArray();
				ImageType outputType = determineOutputType( command.getTransform().getOutputType(), command.getOriginalImageAttributes().getType() );
				command.setExecutionResult( new SimpleImageSource( outputType, bytes ) );
			}

		}
		catch ( Exception e ) {
			LOG.error( "Failed to execute IMOperation: {}", op.toString(), e );
			throw new ImageModificationException( e );
		}
	}

	public IMOperation createIMOperation( ImageTransformCommand command ) {
		ImageAttributes imageAttributes = command.getOriginalImageAttributes();
		ImageTransformDto transform = command.getTransform();

		ImageType outputType = determineOutputType( transform.getOutputType(), imageAttributes.getType() );
		ColorSpaceDto requestedColorSpace = transform.getColorSpace();

		IMOperation op = new IMOperation();
		applyFilter( op );
		double dpiFactor = applyDotsPerInch( op, imageAttributes.getType(), imageAttributes.getDimensions(), transform );

		if ( ImageType.SVG == imageAttributes.getType() ) {
			// read svg on transparent bg
			op.background( "transparent" );
		}

		Integer scene = determineSceneToUse( transform.getScene(), imageAttributes, outputType );
		op.addImage( "-" + ( scene != null ? "[" + scene + "]" : "" ) );

		if ( transform.getCrop() != null ) {
			Crop crop = applyDotsPerInch( DtoUtil.toBusiness( transform.getCrop() ), dpiFactor );
			op.crop( crop.getWidth(), crop.getHeight(), crop.getX(), crop.getY() );
		}

		requestedColorSpace = applyAlphaColorAndAdjustColorSpace( op, transform.getAlphaColor(), requestedColorSpace );

		applyBackgroundColor( op, transform.getBackgroundColor(), imageAttributes.getType(), outputType );
		applyColorspace( op, requestedColorSpace );
		applyResize( op, transform.getWidth(), transform.getHeight(), dpiFactor, imageAttributes.getDimensions() );

		op.p_profile( "*" );
		op.strip();
		op.quality( 1d * ( transform.getQuality() != null ? transform.getQuality() : defaultQuality ) );

		op.addImage( outputType.getExtension() + ":-" );
		return op;
	}

	private Dimensions determineOutputDimensions( Integer width, Integer height, Dimensions originalDimensions ) {
		if ( width == null && height == null ) {
			return originalDimensions;
		}
		return new Dimensions( width != null ? width : 0, height != null ? height : 0 );
	}

	private void applyFilter( IMOperation op ) {
		if ( filter != null ) {
			op.filter( filter );
		}
	}

	private Integer determineSceneToUse( Integer requestedScene, ImageAttributes imageAttributes, ImageType outputType ) {
		if ( requestedScene == null || requestedScene < 0 || requestedScene >= imageAttributes.getSceneCount() ) {
			if ( ImageType.PDF == imageAttributes.getType() && outputType != ImageType.PDF ) {
				// default to first page for pdf to image conversion
				return 0;
			}
		}

		return requestedScene;
	}

	private ColorSpaceDto applyAlphaColorAndAdjustColorSpace( IMOperation op, ColorDto alphaColor, ColorSpaceDto requestedColorSpace ) {
		if ( alphaColor != null ) {
			op.transparent( alphaColor.getValue() );
			if ( requestedColorSpace == null ) {
				requestedColorSpace = ColorSpaceDto.TRANSPARENT;
			}
		}

		return requestedColorSpace;
	}

	private void applyBackgroundColor( IMOperation op, ColorDto requestedColor, ImageType imageType, ImageType outputType ) {
		String backgroundColor = detemineBackgroundColor( requestedColor, imageType, outputType );

		if ( backgroundColor != null ) {
			op.background( backgroundColor );
			op.extent( 0, 0 );
			op.addRawArgs( "+matte" );
		}
	}

	private void applyResize( IMOperation op, Integer width, Integer height, double dpiFactor, Dimensions originalDimensions ) {
		if ( width != null || height != null ) {
			int w = width != null ? width : 0;
			int h = height != null ? height : 0;

			boolean isSmallerThanOriginal = w < originalDimensions.getWidth() && h < originalDimensions.getHeight();

			if ( isSmallerThanOriginal && useThumbnail ) {
				op.thumbnail( width, height, width != null && height != null ? '!' : null );
			}
			else {
				op.resize( width, height, width != null && height != null ? '!' : null );
			}
		}

		if ( dpiFactor != 1d && width == null && height == null ) {
			if ( originalDimensions != null ) {
				op.resize( originalDimensions.getWidth(), originalDimensions.getHeight() );
			}
			else {
				// fallback, resize using percentage in the off chance we don't know the original dimensions
				int downscale = new Long( Math.round( ( 1d / dpiFactor ) * 100 ) ).intValue();
				op.resize( downscale, downscale, "%" );
			}
		}
	}

	private String detemineBackgroundColor( ColorDto backgroundColor, ImageType originalImageType, ImageType outputType ) {
		if ( backgroundColor == null && shouldRemoveTransparency( originalImageType, outputType ) ) {
			return ALPHA_BACKGROUND;
		}
		return backgroundColor != null ? backgroundColor.getValue() : null;
	}

	private void applyColorspace( IMOperation op, ColorSpaceDto requestedColorSpace ) {
		if ( ColorSpaceDto.GRAYSCALE == requestedColorSpace ) {
			op.colorspace( "gray" );
		}
		else if ( ColorSpaceDto.MONOCHROME == requestedColorSpace ) {
			op.monochrome();
		}
		else if ( requestedColorSpace != null ) {
			op.colorspace( requestedColorSpace.name() );
		}
	}

	private ImageType determineOutputType( ImageTypeDto requestedOutputType, ImageType original ) {
		return Optional.ofNullable( requestedOutputType != null ? DtoUtil.toBusiness( requestedOutputType ) : original ).orElse( original );
	}

	private double applyDotsPerInch( IMOperation operation,
	                                 ImageType imageType,
	                                 Dimensions originalDimensions,
	                                 ImageTransformDto transformDto ) {
		Integer requestedDpi = transformDto.getDpi();
		Dimensions outputDimensions = determineOutputDimensions( transformDto.getWidth(), transformDto.getHeight(), originalDimensions );

		if ( supportsDensityArgument( imageType ) ) {
			boolean dpiSpecified = requestedDpi != null;
			int dpi = dpiSpecified ? requestedDpi : adjustDpiToRequestedOutput( defaultDpi, originalDimensions, outputDimensions );
			operation.density( dpi, dpi );
			return dpi / BASE_DPI;
		}

		return 1d;
	}

	private boolean supportsDensityArgument( ImageType imageType ) {
		switch ( imageType ) {
			case PDF:
			case EPS:
				return true;
			default:
				return false;
		}
	}

	private Crop applyDotsPerInch( Crop crop, double dpiFactor ) {
		if ( dpiFactor != 1d ) {
			return new Crop( Double.valueOf( dpiFactor * crop.getX() ).intValue(),
			                 Double.valueOf( dpiFactor * crop.getY() ).intValue(),
			                 Double.valueOf( dpiFactor * crop.getWidth() ).intValue(),
			                 Double.valueOf( dpiFactor * crop.getHeight() ).intValue() );
		}

		return crop;
	}

	private int adjustDpiToRequestedOutput( int dpi, Dimensions originalDimensions, Dimensions outputDimensions ) {
		if ( dpi >= MAX_DPI ) {
			return MAX_DPI;
		}

		double dpiFactor = dpi / BASE_DPI;

		double maxWidth = originalDimensions.getWidth() * dpiFactor;
		double maxHeight = originalDimensions.getHeight() * dpiFactor;
		if ( maxWidth < outputDimensions.getWidth() || maxHeight < outputDimensions.getHeight() ) {
			// image would be zoomed with loss of detail, increase dpi on raster
			return adjustDpiToRequestedOutput( nextDpi( dpi ), originalDimensions, outputDimensions );
		}

		return dpi;
	}

	private int nextDpi( int dpi ) {
		switch ( dpi ) {
			case 72:
				return 96;
			case 96:
				return 150;
			case 150:
				return 300;
			default:
				return dpi + DPI_STEP;
		}
	}

	private boolean shouldRemoveTransparency( ImageType originalImageType, ImageType outputType ) {
		return originalImageType.hasTransparency() && !outputType.hasTransparency();
	}
}
