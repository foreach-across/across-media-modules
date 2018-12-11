package com.foreach.imageserver.core;

import com.foreach.imageserver.core.config.StoreSettings;
import com.foreach.imageserver.core.config.StreamingSettings;
import com.foreach.imageserver.core.config.TransformersSettings;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author Arne Vandamme
 */
@Getter
@Setter
@ConfigurationProperties("imageServerCore")
public class ImageServerCoreModuleSettings
{
	public static final String ROOT_PATH = "imageServerCore.rootPath";
	public static final String IMAGE_SERVER_URL = "imageServerCore.imageServerUrl";
	public static final String STRICT_MODE = "imageServerCore.strictMode";
	public static final String ACCESS_TOKEN = "imageServerCore.accessToken";
	public static final String MD5_HASH_TOKEN = "imageServerCore.md5HashToken";
	public static final String CREATE_LOCAL_CLIENT = "imageServerCore.createLocalClient";

	public static final String PROVIDE_STACKTRACE = "imageServerCore.streaming.provideStackTrace";
	public static final String MAX_BROWSER_CACHE_SECONDS = "imageServerCore.streaming.maxBrowserCacheSeconds";
	public static final String AKAMAI_CACHE_MAX_AGE = "imageServerCore.streaming.akamaiCacheMaxAge";
	public static final String IMAGE_NOT_FOUND_IMAGEKEY = "imageServerCore.streaming.imageNotFoundKey";

	public static final String IMAGE_STORE_FOLDER = "imageServerCore.store.folder";
	public static final String IMAGE_STORE_FOLDER_PERMISSIONS = "imageServerCore.store.folderPermissions";
	public static final String IMAGE_STORE_FILE_PERMISSIONS = "imageServerCore.store.filePermissions";

	public static final String TRANSFORMERS_CONCURRENT_LIMIT = "imageServerCore.transformers.concurrentLimit";

	public static final String IMAGEMAGICK_ENABLED = "imageServerCore.transformers.imageMagick.enabled";
	public static final String IMAGEMAGICK_PRIORITY = "imageServerCore.transformers.imageMagick.priority";
	public static final String IMAGEMAGICK_USE_GRAPHICSMAGICK = "imageServerCore.transformers.imageMagick.useGraphicsMagick";
	public static final String IMAGEMAGICK_USE_GHOSTSCRIPT = "imageServerCore.transformers.imageMagick.useGhostScript";
	public static final String IMAGEMAGICK_PATH = "imageServerCore.transformers.imageMagick.path";

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
	private StoreSettings store = new StoreSettings();
	private TransformersSettings transformers = new TransformersSettings();

	public boolean isStrictMode() {
		return getStrictMode();
	}
}
