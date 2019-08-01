package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.FileManagerModuleSettings;
import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

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
 * Internally this implementation builds on {@link AbstractExpiringFileRepository}, where the actual cached item is the
 * one that expires (and is removed when it expires). See also {@link FileManagerModuleSettings#getExpiration()} for periodic expiration.
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
 * @see ExpiringFileRepository
 * @since 1.4.0
 */
@Slf4j
public class CachingFileRepository extends AbstractExpiringFileRepository<CachedFileResource>
{
	/**
	 * The id of the of the repository which contains the actual cache file resources.
	 * Usually this will refer to a {@link LocalFileRepository} though this is not a requirement.
	 */
	@Getter
	private final String cacheRepositoryId;

	/**
	 * Function used to resolve the file resource which should be used as the resource for the cache.
	 * Takes the original {@link FileDescriptor} and the resolved cache {@link FileRepository} as input.
	 */
	private final BiFunction<FileDescriptor, FileRepository, FileResource> cacheFileResourceResolver;

	private FileManager fileManager;

	@Builder
	private CachingFileRepository( @NonNull FileRepository targetFileRepository,
	                               @NonNull String cacheRepositoryId,
	                               @NonNull BiFunction<FileDescriptor, FileRepository, FileResource> cacheFileResourceResolver,
	                               boolean expireOnShutdown,
	                               boolean expireOnEvict,
	                               int maxItemsToTrack,
	                               @NonNull Function<ExpiringFileResource, Boolean> expirationStrategy ) {
		super( targetFileRepository, expireOnShutdown, expireOnEvict, maxItemsToTrack, expirationStrategy );
		this.cacheRepositoryId = cacheRepositoryId;
		this.cacheFileResourceResolver = cacheFileResourceResolver;
	}

	@Override
	public void setFileManager( FileManager fileManager ) {
		super.setFileManager( fileManager );
		this.fileManager = fileManager;
	}

	@Override
	protected CachedFileResource createExpiringFileResource( FileDescriptor descriptor, FileResource targetFileResource ) {
		FileRepository cacheRepository = fileManager.getRepository( cacheRepositoryId );
		FileResource cacheFileResource = cacheFileResourceResolver.apply( descriptor, cacheRepository );

		return new CachedFileResource( targetFileResource, cacheFileResource, this );
	}

	@Override
	protected void expire( CachedFileResource fileResource ) {
		fileResource.flushCache();
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
	 * By default only 100 file resources are cached and they are removed on {@link #expireTrackedItems()}
	 * if they have not been accessed for longer than one hour.
	 *
	 * @return caching file repository builder
	 * @see #withTranslatedFileDescriptor()
	 */
	@SuppressWarnings("WeakerAccess")
	public static CachingFileRepositoryBuilder withGeneratedFileDescriptor() {
		return builder().cacheFileResourceResolver( ( fd, repository ) -> repository.createFileResource() )
		                .expireOnEvict( true )
		                .expireOnShutdown( true );
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
	 * By default only 100 file resources are cached and they are removed on {@link #expireTrackedItems()}
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
				.expireOnEvict( false )
				.expireOnShutdown( false );
	}

	@SuppressWarnings({ "unused", "squid:S1068" })
	public static class CachingFileRepositoryBuilder
	{
		private String cacheRepositoryId = FileManager.TEMP_REPOSITORY;
		private boolean expireOnShutdown = true;
		private boolean expireOnEvict = true;
		private int maxItemsToTrack = 100;

		public CachingFileRepositoryBuilder() {
			timeBasedExpiration( 60 * 60 * 1000L, 0 );
		}

		/**
		 * Configures a time-based expiration strategy which expires resources
		 * if they have not been accessed for longer than {@code maxUnusedDuration} or if their
		 * creation time was longer than {@code maxAge} ago. Whichever condition matches.
		 * <p/>
		 * Both time durations are in milliseconds. The reference for access time is {@link ExpiringFileResource#getLastAccessTime()},
		 * the reference for age is {@link ExpiringFileResource#getCreationTime()}.
		 * <p/>
		 * Specifying zero or less will skip that condition.
		 *
		 * @param maxUnusedDuration maximum number of milliseconds that is allowed since last access time
		 * @param maxAge            maximum number of milliseconds that is allowed since cache has been created
		 * @return builder
		 */
		@SuppressWarnings({ "UnusedReturnValue" })
		public CachingFileRepositoryBuilder timeBasedExpiration( long maxUnusedDuration, long maxAge ) {
			return expirationStrategy( timeBasedExpirationStrategy( maxUnusedDuration, maxAge ) );
		}
	}
}
