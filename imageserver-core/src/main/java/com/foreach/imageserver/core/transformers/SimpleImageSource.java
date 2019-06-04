package com.foreach.imageserver.core.transformers;

import com.foreach.imageserver.core.business.ImageType;
import org.springframework.core.io.InputStreamSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

public class SimpleImageSource implements ImageSource
{
	private final ImageType imageType;
	private final Supplier<InputStream> inputStreamSupplier;

	public SimpleImageSource( ImageType imageType, InputStreamSource inputStreamSource ) {
		this.imageType = imageType;
		this.inputStreamSupplier = () -> {
			try {
				return inputStreamSource.getInputStream();
			}
			catch ( IOException e ) {
				throw new RuntimeException( e );
			}
		};
	}

	public SimpleImageSource( ImageType imageType, byte[] imageBytes ) {
		this.imageType = imageType;
		this.inputStreamSupplier = () -> new ByteArrayInputStream( imageBytes );
	}

	@Override
	public ImageType getImageType() {
		return imageType;
	}

	@Override
	public InputStream getImageStream() {
		return inputStreamSupplier.get();
	}
}
