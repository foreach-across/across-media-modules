package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FolderDescriptor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Base class for a file repository which tracks its file resources and expires them
 * according to a specific {@link #expirationStrategy}. The actual meaning of expiration
 * depends on the particular implementation.
 * <p/>
 * A target repository is configured which delivers the original file resource that should
 * be adapted into an {@link ExpiringFileResource}. The expiring repository takes the identity
 * of the target repository, developers should only register the expiring repository.
 * <p/>
 * This repository keeps an LRU type map of known file descriptors. The maximum number of
 * resources to track can be configured. Once this maximum is exceeded, the oldest file
 * resource will be evicted; after which point it will be fetched again from the target
 * repository. If a resource expires when evicted depends on the {@link #expireOnEvict} value.
 *
 * @author Arne Vandamme
 * @see ExpiringFileRepository
 * @see CachingFileRepository
 * @since 1.4.0
 */
@Slf4j
@SuppressWarnings("WeakerAccess")
public abstract class AbstractExpiringFileRepository<T extends ExpiringFileResource> extends AbstractFileRepository
{
	/**
	 * The target repository this implementation wraps.
	 */
	@Getter
	private final FileRepository targetFileRepository;

	/**
	 * True if resources should be expired when the file repository is destroyed.
	 */
	@Getter
	private final boolean expireOnShutdown;

	/**
	 * True if resources should be expired when they are evicted.
	 */
	@Getter
	private final boolean expireOnEvict;

	/**
	 * Maximum number of file resources to track. Past this number the least recently fetched file descriptor
	 * will be evicted. Depending on {@link #expireOnEvict} this means the actual resource will be expired immediately.
	 */
	@Getter
	private final int maxItemsToTrack;

	/**
	 * Function which returns {@code true} if a file resource should be expired.
	 */
	@Getter
	private final Function<ExpiringFileResource, Boolean> expirationStrategy;

	private final Map<FileDescriptor, T> trackedResources = Collections.synchronizedMap( new FileResourceCache() );

	protected AbstractExpiringFileRepository( @NonNull FileRepository targetFileRepository,
	                                          boolean expireOnShutdown,
	                                          boolean expireOnEvict,
	                                          int maxItemsToTrack,
	                                          @NonNull Function<ExpiringFileResource, Boolean> expirationStrategy ) {
		super( targetFileRepository.getRepositoryId() );
		this.targetFileRepository = targetFileRepository;
		this.expireOnShutdown = expireOnShutdown;
		this.expireOnEvict = expireOnEvict;
		this.maxItemsToTrack = maxItemsToTrack;
		this.expirationStrategy = expirationStrategy;
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
	public FileDescriptor generateFileDescriptor() {
		return targetFileRepository.generateFileDescriptor();
	}

	/**
	 * Runs all tracked file resources through the expiration strategy, and expires where necessary.
	 **/
	@SuppressWarnings("WeakerAccess")
	public void expireTrackedItems() {
		try {
			LOG.trace( "Running file resource expiration for repository {}", getRepositoryId() );
			new ArrayList<>( trackedResources.keySet() ).forEach( fd -> {
				T fileResource = trackedResources.get( fd );
				if ( fileResource != null && expirationStrategy.apply( fileResource ) ) {
					stopTracking( fd );
					expire( fileResource );
				}
			} );
		}
		catch ( Exception e ) {
			LOG.error( "Exception running file repository expiration", e );
		}
	}

	@Override
	protected void validateFileDescriptor( FileDescriptor descriptor ) {
		// expiring file repository performs no descriptor validation
	}

	@Override
	protected void validateFolderDescriptor( FolderDescriptor descriptor ) {
		// expiring file repository performs no descriptor validation
	}

	@SuppressWarnings("unchecked")
	@Override
	public T createFileResource( boolean allocateImmediately ) {
		return (T) super.createFileResource( allocateImmediately );
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getFileResource( FileDescriptor descriptor ) {
		return (T) super.getFileResource( descriptor );
	}

	@SuppressWarnings("unchecked")
	@Override
	public T createFileResource( File originalFile, boolean deleteOriginal ) throws IOException {
		return (T) super.createFileResource( originalFile, deleteOriginal );
	}

	@SuppressWarnings("unchecked")
	@Override
	public T createFileResource( InputStream inputStream ) throws IOException {
		return (T) super.createFileResource( inputStream );
	}

	@SuppressWarnings("unchecked")
	@Override
	public T createFileResource() {
		return (T) super.createFileResource();
	}

	/**
	 * Shutdown the repository. Depending on the value of {@link #expireOnShutdown} the tracked resources will be expired.
	 */
	@PreDestroy
	@Override
	public void shutdown() {
		trackedResources.values()
		                .forEach( fileResource -> {
			                if ( expirationStrategy.apply( fileResource ) || expireOnShutdown ) {
				                expire( fileResource );
			                }
			                else {
				                evicted( fileResource, false );
			                }
		                } );
		trackedResources.clear();
	}

	@Override
	protected final T buildFileResource( FileDescriptor descriptor ) {
		return trackedResources.computeIfAbsent( descriptor, fd -> {
			FileResource targetFileResource = targetFileRepository.getFileResource( fd );
			return createExpiringFileResource( fd, targetFileResource );
		} );
	}

	protected abstract T createExpiringFileResource( FileDescriptor descriptor, FileResource targetFileResource );

	/**
	 * Eviction notice of a file resource. The second parameter indicates if the resource has expired in the process or not.
	 *
	 * @param fileResource that has been evicted
	 * @param expired      true if the resource has also been expired
	 */
	protected void evicted( T fileResource, boolean expired ) {
	}

	/**
	 * Perform the actual expiration of a resource.
	 *
	 * @param fileResource to expire
	 */
	protected abstract void expire( T fileResource );

	/**
	 * Remove this descriptor from the tracked resources. This does not expire the matching resource.
	 *
	 * @param descriptor to stop tracking
	 */
	protected void stopTracking( FileDescriptor descriptor ) {
		trackedResources.remove( descriptor );
	}

	/**
	 * Run the expiration (removal of stale items) on all {@code AbstractExpiringFileRepository} implementations.
	 * Detects all implementations of {@link AbstractExpiringFileRepository} in the {@link FileRepositoryRegistry}
	 * and executes {@link AbstractExpiringFileRepository#expireTrackedItems()}.
	 *
	 * @param fileRepositoryRegistry that holds the repositories
	 * @see FileRepositoryRegistry
	 */
	public static void expireTrackedItems( @NonNull FileRepositoryRegistry fileRepositoryRegistry ) {
		LOG.trace( "Tracked file resource expiration triggered" );
		fileRepositoryRegistry.listRepositories()
		                      .stream()
		                      .map( r -> r instanceof FileRepositoryDelegate ? ( (FileRepositoryDelegate) r ).getActualImplementation() : r )
		                      .filter( AbstractExpiringFileRepository.class::isInstance )
		                      .map( AbstractExpiringFileRepository.class::cast )
		                      .forEach( AbstractExpiringFileRepository::expireTrackedItems );
	}

	private class FileResourceCache extends LinkedHashMap<FileDescriptor, T>
	{
		FileResourceCache() {
			super( maxItemsToTrack + 1, .75F, true );
		}

		@Override
		protected boolean removeEldestEntry( Map.Entry<FileDescriptor, T> eldest ) {
			boolean shouldEvict = size() > maxItemsToTrack;
			if ( shouldEvict ) {
				if ( expireOnEvict || expirationStrategy.apply( eldest.getValue() ) ) {
					expire( eldest.getValue() );
					evicted( eldest.getValue(), true );
				}
				else {
					evicted( eldest.getValue(), false );
				}
			}

			return shouldEvict;
		}
	}

	/**
	 * Configures a time-based expiration function which expires resources
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
	 * @return expiration function
	 */
	public static Function<ExpiringFileResource, Boolean> timeBasedExpirationStrategy( long maxUnusedDuration, long maxAge ) {
		return fr -> {
			long now = System.currentTimeMillis();

			if ( maxUnusedDuration > 0 && ( now - fr.getLastAccessTime() ) > maxUnusedDuration ) {
				return true;
			}

			if ( maxAge > 0 ) {
				long age = now - fr.getCreationTime();
				return age != now && age > maxAge;
			}

			return false;
		};
	}
}
