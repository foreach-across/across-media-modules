package com.foreach.imageserver.core.config;

import com.foreach.imageserver.core.transformers.imagemagick.ImageMagickSettings;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("imageServerCore.transformers")
public class TransformersSettings
{
	/**
	 * Number of concurrent transformations that can be busy.
	 * <p/>
	 * Type: int, default 10
	 */
	private Integer concurrentLimit = 10;

	/**
	 * Settings related to the imagemagick transformer
	 */
	private ImageMagickSettings imageMagick = new ImageMagickSettings();
}
