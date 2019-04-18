package com.foreach.imageserver.core.transformers.imagemagick;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("imageServerCore.transformers.imageMagick")
public class ImageMagickSettings
{
	/**
	 * True if image magick transformer should be created.
	 */
	private Boolean enabled = true;

	/**
	 * Path to the ImageMagick installation.
	 */
	private String path = "/usr/bin/";

	/**
	 * Priority for the ImageMagick transformer.
	 */
	private Integer priority = 1;

	/**
	 * True if GraphicsMagick should be used instead of regular ImageMagick.
	 */
	private Boolean useGraphicsMagick = false;

	/**
	 * True if ghostscript is supported on the GraphicsMagick installation.
	 */
	private Boolean useGhostScript = false;
}
