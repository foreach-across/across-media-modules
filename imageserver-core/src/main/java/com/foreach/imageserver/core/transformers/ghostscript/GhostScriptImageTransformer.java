package com.foreach.imageserver.core.transformers.ghostscript;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.transformers.*;
import lombok.SneakyThrows;
import org.ghost4j.document.PDFDocument;
import org.ghost4j.renderer.SimpleRenderer;
import org.springframework.core.Ordered;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
@Deprecated
public class GhostScriptImageTransformer implements ImageTransformer
{
	@Override
	public ImageTransformerPriority canExecute( ImageCalculateDimensionsAction action ) {
		return ImageTransformerPriority.UNABLE;// action.getImageSource().getImageType() == ImageType.PDF ? ImageTransformerPriority.PREFERRED : ImageTransformerPriority.UNABLE;
	}

	@Override
	public ImageTransformerPriority canExecute( GetImageAttributesAction action ) {
		return ImageTransformerPriority.UNABLE;
	}

	@Override
	public ImageTransformerPriority canExecute( ImageModifyAction action ) {
		return action.getSourceImageSource().getImageType() == ImageType.PDF ? ImageTransformerPriority.PREFERRED : ImageTransformerPriority.UNABLE;
	}

	@Override
	@SneakyThrows
	public Dimensions execute( ImageCalculateDimensionsAction action ) {
		PDFDocument pdfDocument = new PDFDocument();
		pdfDocument.load( action.getImageSource().getImageStream() );

		return null;
	}

	@Override
	public ImageAttributes execute( GetImageAttributesAction action ) {
		return null;
	}

	@Override
	@SneakyThrows
	public InMemoryImageSource execute( ImageModifyAction action ) {
		PDFDocument pdfDocument = new PDFDocument();
		pdfDocument.load( action.getSourceImageSource().getImageStream() );

		SimpleRenderer renderer = new SimpleRenderer();
		renderer.setResolution( 150 );
		List<Image> images = renderer.render( pdfDocument, 0, 0 );

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write( (RenderedImage) images.get( 0 ), "png", bos );

		return new InMemoryImageSource( ImageType.PNG, bos.toByteArray() );
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
}
