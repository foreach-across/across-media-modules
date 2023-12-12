package com.foreach.imageserver.core.transformers.imagemagick;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("image-server-core.transformers.image-magick")
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

	/**
	 * Default quality setting that should be used when doing transforms.
	 */
	private int defaultQuality = 85;

	/**
	 * Default DPI setting that should be used when processing scalable image formats (eps, pdf, svg).
	 */
	private int defaultDpi = 300;

	/**
	 * Filter that should be used when resizing images.
	 * Can be {@code null} in which case no explicit filter will be set.
	 */
	private String filter = "Box";

	/**
	 * Should the {@code thumbnail} argument be used instead of the {@code resize} argument
	 * when resizing to a smaller image? Thumbnail is faster but might result in lower quality images.
	 */
	private boolean useThumbnail = false;
}
