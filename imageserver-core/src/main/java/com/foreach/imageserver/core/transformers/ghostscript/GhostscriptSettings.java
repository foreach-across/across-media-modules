package com.foreach.imageserver.core.transformers.ghostscript;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
@Data
@ConfigurationProperties(prefix = "image-server-core.transformers.ghostscript")
@SuppressWarnings("WeakerAccess")
public class GhostscriptSettings
{
	private boolean enabled;
	private int priority = 0;
}


