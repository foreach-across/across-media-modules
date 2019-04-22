package com.foreach.imageserver.core.transformers.imagemagick;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.services.DtoUtil;
import com.foreach.imageserver.core.transformers.*;
import com.foreach.imageserver.dto.ImageTransformDto;
import com.foreach.imageserver.logging.LogHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.process.Pipe;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Handles image transforms using ImageMagick.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@Slf4j
public class ImageMagickTransformCommandExecutor extends AbstractOrderedImageCommandExecutor<ImageTransformCommand>
{
	private static final int GS_MAX_DENSITY = 1200;
	private static final int GS_DEFAULT_DENSITY = 72;
	private static final int GS_DENSITY_STEP = 300;

	private static final String ALPHA_BACKGROUND = "white";

	@Setter
	@Getter
	private int defaultQuality = 85;

	@Override
	public ImageTransformerPriority canExecute( ImageTransformCommand command ) {
		return ImageTransformerPriority.FALLBACK;
	}

	@Override
	public void execute( ImageTransformCommand command ) {
		ImageAttributes imageAttributes = command.getOriginalImageAttributes();
		ImageTransformDto transform = command.getTransform();
		ImageType outputType = DtoUtil.toBusiness( transform.getOutputType() );

		if ( outputType == null ) {
			outputType = imageAttributes.getType();
		}

		ConvertCmd cmd = new ConvertCmd();
		IMOperation op = new IMOperation();
		Dimensions appliedPPI = applyPixelsPerInch( op, imageAttributes.getType(), transform.getDpi() );
		op.addImage( "-" );

		String colorspace = "Transparent";

		if ( shouldRemoveTransparency( imageAttributes.getType(), outputType ) ) {
			op.background( ALPHA_BACKGROUND );
			op.flatten();

			colorspace = "RGB";
		}

		Crop crop = applyPixelsPerInch( DtoUtil.toBusiness( transform.getCrop() ), appliedPPI );
		op.crop( crop.getWidth(), crop.getHeight(), crop.getX(), crop.getY() );

		op.units( "PixelsPerInch" );

		op.resize( transform.getWidth(), transform.getHeight(), "!" );
		op.colorspace( colorspace );
		op.strip();
		op.quality( 1d * ( transform.getQuality() != null ? transform.getQuality() : defaultQuality ) );

		// only apply bounding box when available, and when the outputted image is larger than the bounding box
		if ( transform.getMaxWidth() != null ) {
			Dimensions boundaries = new Dimensions( transform.getMaxWidth(), transform.getMaxHeight() );
			if ( boundaries.getWidth() > 0 || boundaries.getHeight() > 0 ) {
				Dimensions output = new Dimensions( transform.getWidth(), transform.getHeight() );
				if ( boundaries.getHeight() < output.getHeight() || boundaries.getWidth() < output.getWidth() ) {
					int height =
							boundaries.getHeight() > 0 && boundaries.getHeight() < output.getHeight() ? boundaries.getHeight() : output.getHeight();
					int width =
							boundaries.getWidth() > 0 && boundaries.getWidth() < output.getWidth() ? boundaries.getWidth() : output.getWidth();
					op.resize( width, height );
				}
			}
		}

		op.addImage( outputType.getExtension() + ":-" );

		try (InputStream imageStream = command.getOriginalImage().getImageStream()) {
			try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
				cmd.setInputProvider( new Pipe( imageStream, null ) );
				cmd.setOutputConsumer( new Pipe( null, os ) );

				cmd.run( op );

				byte[] bytes = os.toByteArray();
				command.setExecutionResult( new InMemoryImageSource( outputType, bytes ) );
			}

		}
		catch ( Exception e ) {
			// todo: fix error logging
			LOG.error(
					"Failed to apply modification - ImageMagickImageTransformer#execute: action={}, appliedDensity={}, crop={}",
					LogHelper.flatten( command ), LogHelper.flatten( appliedPPI ), LogHelper.flatten( crop ), e );
			throw new ImageModificationException( e );
		}
	}

	private Crop applyPixelsPerInch( Crop crop, Dimensions density ) {
		if ( density != null ) {
			double widthFactor = (double) density.getWidth() / GS_DEFAULT_DENSITY;
			double heightFactor = (double) density.getHeight() / GS_DEFAULT_DENSITY;

			return new Crop( Double.valueOf( widthFactor * crop.getX() ).intValue(),
			                 Double.valueOf( heightFactor * crop.getY() ).intValue(),
			                 Double.valueOf( widthFactor * crop.getWidth() ).intValue(),
			                 Double.valueOf( heightFactor * crop.getHeight() ).intValue() );
		}

		return crop;
	}

	private Dimensions applyPixelsPerInch( IMOperation operation, ImageType imageType, Integer dpi ) {
		if ( imageType.isScalable() ) {
			Dimensions density = dpi != null ? new Dimensions( dpi, dpi ) : null;

			if ( density != null && ( density.getHeight() > 1 || density.getWidth() > 1 ) ) {
				int horizontalDensity = calculateDensity( density.getWidth() );
				int verticalDensity = calculateDensity( density.getHeight() );

				LOG.debug( "Applying density {}x{}", horizontalDensity, verticalDensity );
				operation.density( horizontalDensity, verticalDensity );

				return new Dimensions( horizontalDensity, verticalDensity );
			}
		}

		return null;
	}

	private int calculateDensity( int multiplier ) {
		int raw = Math.min( GS_MAX_DENSITY, GS_DEFAULT_DENSITY * Math.max( multiplier, 1 ) );
		int times = raw / GS_DENSITY_STEP;
		int remainder = raw % GS_DENSITY_STEP;

		return remainder == 0 ? raw : Math.min( GS_MAX_DENSITY, ( times + 1 ) * GS_DENSITY_STEP );
	}

	private boolean shouldRemoveTransparency( ImageType originalImageType, ImageType outputType ) {
		return originalImageType.hasTransparency() && !outputType.hasTransparency();
	}
}
