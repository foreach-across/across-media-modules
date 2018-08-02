package com.foreach.imageserver.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("imageServerCore.transformers")
@Getter
@Setter
public class TransformersSettings
{
	/**
	 * Number of concurrent transformations that can be busy.
	 * <p/>
	 * Type: int, default 10
	 */
	private Integer concurrentLimit = 10;

	/**
	 * True if image magick transformer should be created.
	 */
	private Boolean imageMagickEnabled = true;

	/**
	 * Path to the ImageMagick installation.
	 */
	private String imageMagickPath = "/usr/bin/";

	/**
	 * Priority for the ImageMagick transformer.
	 */
	private Integer imageMagickPriority = 1;

	/**
	 * True if GraphicsMagick should be used instead of regular ImageMagick.
	 */
	private Boolean useGraphicsMagick = false;

	/**
	 * True if ghostscript is supported on the GraphicsMagick installation.
	 */
	private Boolean useGhostScript = false;

}
