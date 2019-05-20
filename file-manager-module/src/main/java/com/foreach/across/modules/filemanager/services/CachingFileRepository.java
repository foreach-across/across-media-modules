package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * {@link FileRepository} implementation which deals out cached file resources.
 * Wraps around an existing file repository and will take its identity.
 * When a resource is fetched, a cache resource will be created as well from another repository,
 * identified by {@link #getCacheRepositoryId()}. A {@link CachedFileResource} will then be returned
 * which caches the target file resource in the one returned from the cache repository.
 * <p/>
 * This is mainly useful when you know that a file resource will be used several times once it is
 * fetched, and the actual target repository is slow (usually a remote repository, for example {@link AmazonS3FileRepository}).
 * Using a fast {@link LocalFileRepository} as cache repository can gain significant performance improvement.
 * <p/>
 * Internally this implementation uses a form of LRU map to evict items when the maximum size has been reached.
 * Additionally you can run {@link #cleanupCache()} to remove stale items from the cache. It might be best practice to
 * execute this method periodically. See also {@link com.foreach.across.modules.filemanager.FileManagerModuleSettings.CacheCleanupProperties}.
 * <p/>
 * If file resource updates happen only through the {@link FileManager}, keeping target and cache in sync should
 * not be much of a problem. Multi-instance applications can use a {@link #withTranslatedFileDescriptor()} strategy
 * to ensure optimal cache use (for example with shared network storage).
 * <p/>
 * NOTE: Developers should only register the caching version of the repository in their application.
 *
 * @author Arne Vandamme
 * @see CachedFileResource
 * @see #withGeneratedFileDescriptor()
 * @see #withTranslatedFileDescriptor()
 * @since 1.4.0
 */
@Slf4j
public class CachingFileRepository extends AbstractFileRepository
{
	/**
	 * The target repository this implementation wraps.
	 */
	@Getter
	private final FileRepository targetFileRepository;

	/**
	 * The id of the of the repository which contains the actual cache file resources.
	 * Usually this will refer to a {@link LocalFileRepository} though this is not a requirement.
	 */
	@Getter
	private final String cacheRepositoryId;

	/**
	 * True if cache items should be removed when the file repository is destroyed.
	 */
	@Getter
	private final boolean removeCacheOnShutdown;

	/**
	 * True if cache items should be removed when they are evicted.
	 */
	@Getter
	private final boolean removeCacheOnEvict;

	/**
	 * Maximum number of file resources to cache. Past this number the least recently fetched file descriptor
	 * will be evicted. Depending on {@link #removeCacheOnEvict} this means the actual cache resource will be removed.
	 */
	@Getter
	private final int maxCacheItems;

	/**
	 * Function used to resolve the file resource which should be used as the resource for the cache.
	 * Takes the original {@link FileDescriptor} and the resolved cache {@link FileRepository} as input.
	 */
	private final BiFunction<FileDescriptor, FileRepository, FileResource> cacheFileResourceResolver;

	/**
	 * Function which returns {@code true} if a file resource should be removed.
	 */
	private final Function<CachedFileResource, Boolean> removalStrategy;

	private FileManager fileManager;

	private final Map<FileDescriptor, CachedFileResource> cachedResources = Collections.synchronizedMap( new FileResourceCache() );

	@Builder(toBuilder = true)
	private CachingFileRepository( @NonNull FileRepository targetFileRepository,
	                               @NonNull String cacheRepositoryId,
	                               @NonNull BiFunction<FileDescriptor, FileRepository, FileResource> cacheFileResourceResolver,
	                               boolean removeCacheOnShutdown,
	                               boolean removeCacheOnEvict,
	                               int maxCacheItems,
	                               @NonNull Function<CachedFileResource, Boolean> removalStrategy ) {
		super( targetFileRepository.getRepositoryId() );
		this.targetFileRepository = targetFileRepository;
		this.cacheRepositoryId = cacheRepositoryId;
		this.cacheFileResourceResolver = cacheFileResourceResolver;
		this.removeCacheOnShutdown = removeCacheOnShutdown;
		this.removeCacheOnEvict = removeCacheOnEvict;
		this.maxCacheItems = maxCacheItems;
		this.removalStrategy = removalStrategy;
	}

	@Override
	public String getRepositoryId() {
		return targetFileRepository.getRepositoryId();
	}

	@Override
	protected PathGenerator getPathGenerator() {
		return targetFileRepository instanceof AbstractFileRepository
				? ( (AbstractFileRepository) targetFileRepository ).getPathGenerator() : null;
	}

	@Override
	public void setPathGenerator( PathGenerator pathGenerator ) {
		if ( targetFileRepository instanceof AbstractFileRepository ) {
			( (AbstractFileRepository) targetFileRepository ).setPathGenerator( pathGenerator );
		}
		else {
			throw new UnsupportedOperationException( "Target file repository does not implement AbstractFileRepository: path generator is not supported" );
		}
	}

	@Override
	public void setFileManager( FileManager fileManager ) {
		super.setFileManager( fileManager );
		this.fileManager = fileManager;
	}

	@Override
	public FileDescriptor generateFileDescriptor() {
		return targetFileRepository.generateFileDescriptor();
	}

	@Override
	protected void validateFileDescriptor( FileDescriptor descriptor ) {
		// caching file repository performs no descriptor validation
	}

	@Override
	protected FileResource buildFileResource( FileDescriptor descriptor ) {
		return cachedResources.computeIfAbsent( descriptor, fd -> {
			FileResource targetFileResource = targetFileRepository.getFileResource( fd );

			FileRepository cacheRepository = fileManager.getRepository( cacheRepositoryId );
			FileResource cacheFileResource = cacheFileResourceResolver.apply( fd, cacheRepository );

			return new CachedFileResource( targetFileResource, cacheFileResource );
		} );
	}

	/**
	 * Runs all cached file resources through the removal strategy, and removes the stale items.
	 **/
	@SuppressWarnings("WeakerAccess")
	public void cleanupCache() {
		try {
			LOG.trace( "Cleaning up cache for repository {}", getRepositoryId() );
			new ArrayList<>( cachedResources.keySet() ).forEach( fd -> {
				CachedFileResource fileResource = cachedResources.get( fd );
				if ( fileResource != null && removalStrategy.apply( fileResource ) ) {
					cachedResources.remove( fd );
					fileResource.flushCache();
				}
			} );
		}
		catch ( Exception e ) {
			LOG.error( "Exception running cache eviction", e );
		}

	}

	/**
	 * Shutdown the repository. Depending on the value of {@link #removeCacheOnShutdown} the cached
	 * resources will be removed.
	 */
	@PreDestroy
	public void shutdown() {
		cachedResources.values()
		               .forEach( fileResource -> {
			               if ( removalStrategy.apply( fileResource ) || removeCacheOnShutdown ) {
				               fileResource.flushCache();
			               }
		               } );
		cachedResources.clear();
	}

	/**
	 * Pre-configures a caching file repository that generates a new file resource in the cache repository.
	 * The cache file resource will always be different, even for the same target file resource.
	 * This means that across applications or VM restarts, new cache resources will be created.
	 * The builder is also preconfigured to remove all cached files on shutdown or eviction.
	 * <p/>
	 * This strategy is the best approach if you are not using shared file storage across applications,
	 * or if you explicitly do not want to reuse pre-existing cache files.
	 * See {@link #withTranslatedFileDescriptor()} if you want to do the exact opposite.
	 * <p/>
	 * By default only 100 file resources are cached and they are removed on {@link #cleanupCache()}
	 * if they have not been accessed for longer than one hour.
	 *
	 * @return caching file repository builder
	 * @see #withTranslatedFileDescriptor()
	 */
	@SuppressWarnings("WeakerAccess")
	public static CachingFileRepositoryBuilder withGeneratedFileDescriptor() {
		return builder().cacheFileResourceResolver( ( fd, repository ) -> repository.createFileResource() )
		                .removeCacheOnEvict( true )
		                .removeCacheOnShutdown( true );
	}

	/**
	 * Pre-configures a caching file repository which translates the target file descriptor into
	 * a file descriptor in the cache repository. Both folder id and file name of the target file descriptor
	 * will be kept. This means that the same target file descriptor will always result in the same cache file descriptor.
	 * This builder is also preconfigured to <strong>NOT</strong> remove the actual cached files on shutdown or evictions.
	 * It is assumed they will be reused whenever the target file descriptor is requested again.
	 * <p/>
	 * Use this strategy if you have multiple applications and want to share the same cache file storage across them,
	 * or if you want to reuse pre-existing cache files (useful for long-lived, rarely changing files).
	 * See {@link #withGeneratedFileDescriptor()} if you want to have a more aggressive strategy which
	 * renews its cache file resources more frequently.
	 * <p/>
	 * By default only 100 file resources are cached and they are removed on {@link #cleanupCache()}
	 * if they have not been accessed for longer than one hour.
	 *
	 * @return caching file repository builder
	 * @see #withGeneratedFileDescriptor()
	 */
	@SuppressWarnings("WeakerAccess")
	public static CachingFileRepositoryBuilder withTranslatedFileDescriptor() {
		return builder()
				.cacheFileResourceResolver(
						( fd, repository ) ->
								repository.getFileResource( FileDescriptor.of( repository.getRepositoryId(), fd.getFolderId(), fd.getFileId() ) )
				)
				.removeCacheOnEvict( false )
				.removeCacheOnShutdown( false );
	}

	/**
	 * Run the cache cleanup (removal of stale items) on all caching file repositories.
	 * Detects all implementations of {@link CachingFileRepository} in the {@link FileRepositoryRegistry}
	 * and executes {@link CachingFileRepository#cleanupCache()}.
	 *
	 * @param fileRepositoryRegistry that holds the repositories
	 * @see FileRepositoryRegistry
	 */
	public static void cleanupCaches( @NonNull FileRepositoryRegistry fileRepositoryRegistry ) {
		LOG.trace( "Caches cleanup triggered" );
		fileRepositoryRegistry.listRepositories()
		                      .stream()
		                      .map( r -> r instanceof FileRepositoryDelegate ? ( (FileRepositoryDelegate) r ).getActualImplementation() : r )
		                      .filter( CachingFileRepository.class::isInstance )
		                      .map( CachingFileRepository.class::cast )
		                      .forEach( CachingFileRepository::cleanupCache );
	}

	private class FileResourceCache extends LinkedHashMap<FileDescriptor, CachedFileResource>
	{
		FileResourceCache() {
			super( maxCacheItems + 1, .75F, true );
		}

		@Override
		protected boolean removeEldestEntry( Map.Entry<FileDescriptor, CachedFileResource> eldest ) {
			boolean shouldEvict = size() > maxCacheItems;
			if ( shouldEvict && ( removeCacheOnEvict || removalStrategy.apply( eldest.getValue() ) ) ) {
				eldest.getValue().flushCache();
			}
			return shouldEvict;
		}
	}

	@SuppressWarnings({ "unused", "squid:S1068" })
	public static class CachingFileRepositoryBuilder
	{
		private String cacheRepositoryId = FileManager.TEMP_REPOSITORY;
		private boolean removeCacheOnShutdown = true;
		private boolean removeCacheOnEvict = true;
		private int maxCacheItems = 100;

		public CachingFileRepositoryBuilder() {
			timeBasedRemoval( 60 * 60 * 1000L, 0 );
		}

		/**
		 * Configures a time-based {@link #removalStrategy(Function)} which removes cached resources
		 * if they have not been accessed for longer than {@code maxUnusedDuration} or if their
		 * cache version was created longer than {@code maxAge} ago. Whichever condition matches.
		 * <p/>
		 * Both time durations are in milliseconds. The reference for access time is {@link CachedFileResource#getLastAccessTime()},
		 * the reference for age is {@link CachedFileResource#getCacheCreationTime()}.
		 * <p/>
		 * Specifying zero or less will skip that condition.
		 *
		 * @param maxUnusedDuration maximum number of milliseconds that is allowed since last access time
		 * @param maxAge            maximum number of milliseconds that is allowed since cache has been created
		 * @return builder
		 */
		@SuppressWarnings({ "UnusedReturnValue" })
		public CachingFileRepositoryBuilder timeBasedRemoval( long maxUnusedDuration, long maxAge ) {
			return removalStrategy( timeBasedRemovalStrategy( maxUnusedDuration, maxAge ) );
		}

		static Function<CachedFileResource, Boolean> timeBasedRemovalStrategy( long maxUnusedDuration, long maxAge ) {
			return fr -> {
				long now = System.currentTimeMillis();

				if ( maxUnusedDuration > 0 && ( now - fr.getLastAccessTime() ) > maxUnusedDuration ) {
					return true;
				}

				if ( maxAge > 0 ) {
					long age = now - fr.getCacheCreationTime();
					return age != now && age > maxAge;
				}

				return false;
			};
		}
	}
}
