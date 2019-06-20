package com.foreach.imageserver.core.transformers.imagemagick;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.transformers.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.im4java.core.Info;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Resolves {@link ImageAttributes} for image files using ImageMagick.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@Slf4j
public class ImageMagickAttributesCommandExecutor extends AbstractOrderedImageCommandExecutor<ImageAttributesCommand>
{
	private final static Map<String, ImageType> magickToImageType = new HashMap<>( 24 );

	static {
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
		magickToImageType.put( "BMP", ImageType.BMP );

		// This seems to be a bug in GraphicsMagick.
		magickToImageType.put( "MVG", ImageType.SVG );
	}

	public ImageMagickAttributesCommandExecutor() {
		for ( ImageType imageType : ImageType.values() ) {
			if ( !magickToImageType.containsValue( imageType ) ) {
				throw new RuntimeException( String.format( "No magick known for image type %s", imageType ) );
			}
		}
	}

	@Override
	public ImageTransformerPriority canExecute( ImageAttributesCommand command ) {
		return ImageTransformerPriority.PREFERRED;
	}

	@Override
	public void execute( ImageAttributesCommand command ) {
		try (InputStream stream = command.getImageStream()) {
			Info info = new Info( "-", stream, false );
			ImageType imageType = resolveImageType( info );
			Dimensions dimensions = new Dimensions( info.getImageWidth(), info.getImageHeight() );
			command.setExecutionResult( new ImageAttributes( imageType, dimensions, info.getSceneCount() ) );
		}
		catch ( Exception e ) {
			throw new ImageModificationException( e );
		}
	}

	private ImageType resolveImageType( Info imageInfo ) {
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
