package com.foreach.imageserver.core.transformers;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.logging.LogHelper;
import org.apache.commons.lang3.StringUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.Info;
import org.im4java.process.Pipe;
import org.im4java.process.ProcessStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

// TODO Support for vector formats using Ghostscript is untested.
@Component
public class ImageMagickImageTransformer implements ImageTransformer
{
	public static final int GS_MAX_DENSITY = 1200;
	public static final int GS_DEFAULT_DENSITY = 72;
	public static final int GS_DENSITY_STEP = 300;
	public static final String ALPHA_BACKGROUND = "white";
	public static final double QUALITY = 85;

	private static final Logger LOG = LoggerFactory.getLogger( ImageMagickImageTransformer.class );

	private final int order;
	private final boolean ghostScriptEnabled;

	// ImageMagick uses an ASCII string known as magick (e.g. GIF) to identify file formats, algorithms acting as
	// formats, built-in patterns, and embedded profile types (See: http://www.imagemagick.org/script/formats.php).
	private final Map<String, ImageType> magickToImageType;

	public ImageMagickImageTransformer( int order,
	                                    String imageMagickPath,
	                                    boolean ghostScriptEnabled,
	                                    boolean useGraphicsMagick ) {
		this.order = order;
		this.ghostScriptEnabled = ghostScriptEnabled;

		ProcessStarter.setGlobalSearchPath( new File( imageMagickPath ).getAbsolutePath() );

		/**
		 * I'd rather not set a global system property for this, but this is the only way to have Info use
		 * GraphicsMagick.
		 */
		if ( useGraphicsMagick ) {
			System.setProperty( "im4java.useGM", "true" );
		}

		magickToImageType = new HashMap<>( 24 );
		magickToImageType.put( "JPEG", ImageType.JPEG );
		magickToImageType.put( "PNG", ImageType.PNG );
		magickToImageType.put( "PNG8", ImageType.PNG );
		magickToImageType.put( "PNG00", ImageType.PNG );
		magickToImageType.put( "PNG24", ImageType.PNG );
		magickToImageType.put( "PNG32", ImageType.PNG );
		magickToImageType.put( "PNG48", ImageType.PNG );
		magickToImageType.put( "PNG64", ImageType.PNG );
		magickToImageType.put( "GIF", ImageType.GIF );
		magickToImageType.put( "SVG", ImageType.SVG );
		magickToImageType.put( "EPS", ImageType.EPS );
		magickToImageType.put( "EPI", ImageType.EPS );
		magickToImageType.put( "EPS2", ImageType.EPS );
		magickToImageType.put( "EPS3", ImageType.EPS );
		magickToImageType.put( "EPSF", ImageType.EPS );
		magickToImageType.put( "EPSI", ImageType.EPS );
		magickToImageType.put( "EPT", ImageType.EPS );
		magickToImageType.put( "PS", ImageType.EPS );
		magickToImageType.put( "PS2", ImageType.EPS );
		magickToImageType.put( "PS3", ImageType.EPS );
		magickToImageType.put( "PDF", ImageType.PDF );
		magickToImageType.put( "EPDF", ImageType.PDF );
		magickToImageType.put( "TIFF", ImageType.TIFF );

		// This seems to be a bug in GraphicsMagick.
		magickToImageType.put( "MVG", ImageType.SVG );

		for ( ImageType imageType : ImageType.values() ) {
			if ( !magickToImageType.containsValue( imageType ) ) {
				throw new RuntimeException( String.format( "No magick known for image type %s", imageType ) );
			}
		}
	}

	@Override
	public ImageTransformerPriority canExecute( ImageCalculateDimensionsAction action ) {
		return canExecute( action.getImageSource().getImageType() );
	}

	@Override
	public ImageTransformerPriority canExecute( GetImageAttributesAction action ) {
		return ImageTransformerPriority.PREFERRED;
	}

	@Override
	public ImageTransformerPriority canExecute( ImageModifyAction action ) {
		return canExecute( action.getSourceImageSource().getImageType() );
	}

	@Override
	public Dimensions execute( ImageCalculateDimensionsAction action ) {
		if ( canExecute( action ) == ImageTransformerPriority.UNABLE ) {
			throw new UnsupportedOperationException();
		}

		StreamImageSource imageSource = action.getImageSource();
		try( InputStream stream = imageSource.getImageStream() ) {

			Info info = new Info( "-", stream, false );
			return new Dimensions( info.getImageWidth(), info.getImageHeight() );
		}
		catch ( Exception e ) {
			LOG.error( "Failed to get image dimensions - ImageMagickImageTransformer#execute: action={}",
			           LogHelper.flatten( action ), e );
			throw new ImageModificationException( e );
		}
	}

	@Override
	public ImageAttributes execute( GetImageAttributesAction action ) {
		if ( canExecute( action ) == ImageTransformerPriority.UNABLE ) {
			throw new UnsupportedOperationException();
		}

		ImageType imageType = null;
		Dimensions dimensions = null;
		try( InputStream stream = action.getImageStream() ) {
			Info info = new Info( "-", stream, false );
			imageType = toImageType( info );
			dimensions = new Dimensions( info.getImageWidth(), info.getImageHeight() );
			return new ImageAttributes( imageType, dimensions );
		}
		catch ( Exception e ) {
			LOG.error(
					"Failed to get image attributes - ImageMagickImageTransformer#execute: action={}, imageType={}, dimensions={}",
					LogHelper.flatten( action ), LogHelper.flatten( imageType ), LogHelper.flatten( dimensions ), e );
			throw new ImageModificationException( e );
		}
	}

	@Override
	public InMemoryImageSource execute( ImageModifyAction action ) {
		if ( canExecute( action ) == ImageTransformerPriority.UNABLE ) {
			throw new UnsupportedOperationException();
		}

		ConvertCmd cmd = new ConvertCmd();
		IMOperation op = new IMOperation();
		Dimensions appliedDensity = setDensityIfRequired( op, action );
		op.addImage( "-" );

		String colorspace = "Transparent";

		if ( shouldRemoveTransparency( action ) ) {
			op.background( ALPHA_BACKGROUND );
			op.flatten();

			colorspace = "RGB";
		}

		Crop crop = applyDensity( action.getCrop(), appliedDensity );
		op.crop( crop.getWidth(), crop.getHeight(), crop.getX(), crop.getY() );

		op.units( "PixelsPerInch" );

		op.resize( action.getOutputDimensions().getWidth(), action.getOutputDimensions().getHeight(), "!" );
		op.colorspace( colorspace );
		op.strip();
		op.quality( QUALITY );

		// only apply bounding box when available, and when the outputted image is larger than the bounding box
		Dimensions boundaries = action.getBoundaries();
		if (boundaries != null && boundaries.getWidth() > 0 && boundaries.getHeight() > 0){
			Dimensions output = action.getOutputDimensions();
			if (boundaries.getHeight() < output.getHeight() || boundaries.getWidth() < output.getWidth()){
				op.resize( action.getBoundaries().getWidth(), action.getBoundaries().getHeight() );
			}
		}

		op.addImage( action.getOutputType().getExtension() + ":-" );

		try( InputStream imageStream = action.getSourceImageSource().getImageStream() ) {
			try ( ByteArrayOutputStream os = new ByteArrayOutputStream() ) {
				cmd.setInputProvider( new Pipe( imageStream, null ) );
				cmd.setOutputConsumer( new Pipe( null, os ) );

				cmd.run( op );

				byte[] bytes = os.toByteArray();
				return new InMemoryImageSource( action.getOutputType(), bytes );
			}

		}
		catch ( Exception e ) {
			LOG.error(
					"Failed to apply modification - ImageMagickImageTransformer#execute: action={}, appliedDensity={}, crop={}",
					LogHelper.flatten( action ), LogHelper.flatten( appliedDensity ), LogHelper.flatten( crop ), e );
			throw new ImageModificationException( e );
		}
	}

	@Override
	public int getOrder() {
		return order;
	}

	private ImageTransformerPriority canExecute( ImageType imageType ) {
		if ( ( imageType == ImageType.EPS || imageType == ImageType.PDF ) && !ghostScriptEnabled ) {
			return ImageTransformerPriority.UNABLE;
		}
		else {
			return ImageTransformerPriority.PREFERRED;
		}
	}

	private Crop applyDensity( Crop crop, Dimensions density ) {
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

	private Dimensions setDensityIfRequired( IMOperation operation, ImageModifyAction action ) {
		if ( action.getSourceImageSource().getImageType().isScalable() ) {
			Dimensions density = action.getDensity();

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

	private boolean shouldRemoveTransparency( ImageModifyAction action ) {
		return action.getSourceImageSource().getImageType().hasTransparency() && !action.getOutputType().hasTransparency();
	}

	private ImageType toImageType( Info imageInfo ) {
		String reportedFormatString = imageInfo.getImageFormat();
		if ( StringUtils.isBlank( reportedFormatString ) ) {
			throw new ImageModificationException( "The image format could not be determined." );
		}

		// The image format is written out in full next to the magick.
		String magick = reportedFormatString.split( "\\s" )[0];

		ImageType imageType = magickToImageType.get( magick );
		if ( imageType == null ) {
			throw new ImageModificationException( String.format( "Image type %s is not supported.", magick ) );
		}

		return imageType;
	}
}
