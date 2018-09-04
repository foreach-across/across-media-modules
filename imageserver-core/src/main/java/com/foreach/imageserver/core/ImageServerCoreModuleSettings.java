package com.foreach.imageserver.core;

import com.foreach.imageserver.core.config.StoreSettings;
import com.foreach.imageserver.core.config.StreamingSettings;
import com.foreach.imageserver.core.config.TransformersSettings;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Arne Vandamme
 */
@Getter
@Setter
@ConfigurationProperties("image-server-core")
public class ImageServerCoreModuleSettings
{
	public static final String ROOT_PATH = "image-server-core.root-path";
	public static final String IMAGE_SERVER_URL = "image-server-core.image-server-url";
	public static final String STRICT_MODE = "image-server-core.strict-mode";
	public static final String ACCESS_TOKEN = "image-server-core.access-token";
	public static final String MD5_HASH_TOKEN = "image-server-core.md5-hash-token";
	public static final String CREATE_LOCAL_CLIENT = "image-server-core.create-local-client";

	public static final String PROVIDE_STACKTRACE = "image-server-core.streaming.provide-stack-trace";
	public static final String MAX_BROWSER_CACHE_SECONDS = "image-server-core.streaming.max-browser-cache-seconds";
	public static final String AKAMAI_CACHE_MAX_AGE = "image-server-core.streaming.akamai-cache-max-age";
	public static final String IMAGE_NOT_FOUND_IMAGEKEY = "image-server-core.streaming.image-not-found-key";

	public static final String IMAGE_STORE_FOLDER = "image-server-core.store.folder";
	public static final String IMAGE_STORE_FOLDER_PERMISSIONS = "image-server-core.store.folder-permissions";
	public static final String IMAGE_STORE_FILE_PERMISSIONS = "image-server-core.store.file-permissions";

	public static final String TRANSFORMERS_CONCURRENT_LIMIT = "image-server-core.transformers.concurrent-limit";

	public static final String IMAGEMAGICK_ENABLED = "image-server-core.transformers.image-magick-enabled";
	public static final String IMAGEMAGICK_PRIORITY = "image-server-core.transformers.image-magick-priority";
	public static final String IMAGEMAGICK_USE_GRAPHICSMAGICK = "image-server-core.transformers.use-graphics-magick";
	public static final String IMAGEMAGICK_USE_GHOSTSCRIPT = "image-server-core.transformers.imageMagick.use-ghost-script";
	public static final String IMAGEMAGICK_PATH = "image-server-core.transformers.image-magick-path";

	/**
	 * Base path for all controllers.
	 */
	private String rootPath = "";

	/**
	 * URL for this ImageServer instance.  In case a local client will be created, this
	 * will be the base url for requesting images.
	 */
	private String imageServerUrl;

	/**
	 * Should ImageServer be operating in strict mode.  If so any
	 * {@link com.foreach.imageserver.client.ImageRequestHashBuilder} will be ignored and only requests for registered
	 * resolutions will be accepted.  This matches the behaviour of ImageServer pre-4.x.x.  Enabling strict mode can
	 * have a slight performance gain.
	 * <p/>
	 * Type: boolean, default: false
	 */
	private Boolean strictMode = false;

	/**
	 * Access token required for secured services.
	 * <p/>
	 * Type: string
	 */
	private String accessToken = "azerty";

	/**
	 * Token to use for a default {@link com.foreach.imageserver.client.Md5ImageRequestHashBuilder}.
	 * Will only be used if ImageServer is not in strict mode.
	 * <p/>
	 * Be aware that changing the hash token or security hash mechanism can render all existing image urls
	 * for non-registered resolutions invalid.
	 */
	private String md5HashToken;

	/**
	 * Should a {@link com.foreach.imageserver.core.client.LocalImageServerClient} instance be created
	 * for this ImageServer.  If true the instance will be created and exposed so dependant modules can use it.
	 * <p/>
	 * Type: boolean, default: false
	 */
	private Boolean createLocalClient = false;

	private StreamingSettings streaming = new StreamingSettings();
	private StoreSettings storeSettings = new StoreSettings();
	private TransformersSettings transformers = new TransformersSettings();

	public boolean isStrictMode() {
		return getStrictMode();
	}
}
