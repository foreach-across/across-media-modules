package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FolderResource;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.function.Function;

/**
 * {@link FileRepository} implementation which deals out file resources
 * that expire according to a specified strategy. This implementation
 * takes the identity of a target file repository and wraps its resources
 * with the expiration mechanics.
 * <p/>
 * When a resource expires it will automatically be deleted by calling {@link FileResource#delete()}.
 * <p/>
 * This implementation is especially useful for temporary file resources where you want
 * the application itself to handle automatic cleanup (for example by wrapping a {@link LocalFileRepository}
 * as an {@code ExpiringFileRepository}). Temporary file resources usually have a generated file descriptor.
 * <p/>
 * NOTE: Developers should only register the expiring version of the repository in their application.
 *
 * @author Arne Vandamme
 * @see CachingFileRepository
 * @since 1.4.0
 */
@Slf4j
public class ExpiringFileRepository extends AbstractExpiringFileRepository<ExpiringFileRepository.TrackedResource>
{
	@Builder
	private ExpiringFileRepository( @NonNull FileRepository targetFileRepository,
	                                boolean expireOnShutdown,
	                                boolean expireOnEvict,
	                                int maxItemsToTrack,
	                                @NonNull Function<ExpiringFileResource, Boolean> expirationStrategy ) {
		super( targetFileRepository, expireOnShutdown, expireOnEvict, maxItemsToTrack, expirationStrategy );
	}

	protected ExpiringFileRepository.TrackedResource createExpiringFileResource( FileDescriptor descriptor, FileResource targetFileResource ) {
		return new TrackedResource( targetFileResource );
	}

	protected void expire( ExpiringFileRepository.TrackedResource fileResource ) {
		try {
			if ( fileResource.exists() ) {
				fileResource.deleteInternal();
			}
		}
		catch ( Exception e ) {
			LOG.error( "Error not expiring file resource {}", fileResource.getDescriptor(), e );
		}
	}

	@Override
	protected void evicted( ExpiringFileRepository.TrackedResource fileResource, boolean expired ) {
		if ( !expired ) {
			fileResource.evictedNonExpired = true;
		}
	}

	@RequiredArgsConstructor
	protected class TrackedResource implements ExpiringFileResource
	{
		@NonNull
		private final FileResource target;

		@Getter
		private long lastAccessTime = System.currentTimeMillis();

		private boolean evictedNonExpired = false;

		@Override
		public FolderResource getFolderResource() {
			return ExpiringFileRepository.this.createExpiringFolderResource( target.getFolderResource() );
		}

		@Override
		public long getCreationTime() {
			try {
				return target.lastModified();
			}
			catch ( IOException ignore ) {
				return 0;
			}
		}

		@Override
		public FileDescriptor getDescriptor() {
			return target().getDescriptor();
		}

		@Override
		public boolean delete() {
			ExpiringFileRepository.this.stopTracking( getDescriptor() );
			return deleteInternal();
		}

		private boolean deleteInternal() {
			return target().delete();
		}

		@Override
		public FileResource createRelative( String relativePath ) {
			throw new UnsupportedOperationException( "creating relative path is not yet supported" );
		}

		@Override
		public boolean isWritable() {
			return target().isWritable();
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return target().getOutputStream();
		}

		@Override
		public boolean exists() {
			return target().exists();
		}

		@Override
		public boolean isReadable() {
			return target().isReadable();
		}

		@Override
		public boolean isOpen() {
			return target().isOpen();
		}

		@Override
		public URL getURL() throws IOException {
			return target().getURL();
		}

		@Override
		public URI getURI() {
			return target().getURI();
		}

		@Override
		public long contentLength() throws IOException {
			return target().contentLength();
		}

		@Override
		public long lastModified() throws IOException {
			return target().lastModified();
		}

		@Override
		public String getFilename() {
			return target().getFilename();
		}

		@Override
		public String getDescription() {
			return "axfs expiring resource (" + target().getDescription() + ")";
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return target().getInputStream();
		}

		@Override
		public boolean equals( Object obj ) {
			return obj == this || ( obj instanceof FileResource && target.equals( obj ) );
		}

		@Override
		public int hashCode() {
			return target.hashCode();
		}

		@Override
		@SuppressWarnings("squid:ObjectFinalizeOverridenCheck")
		protected void finalize() {
			try {
				// fallback - attempt to remove the target resource on GC
				if ( evictedNonExpired && target.exists() ) {
					target.delete();
				}
			}
			catch ( Exception ignore ) {
				// ignore any thrown exceptions
			}
		}

		private FileResource target() {
			lastAccessTime = System.currentTimeMillis();
			return target;
		}
	}

	@SuppressWarnings({ "unused", "squid:S1068" })
	public static class ExpiringFileRepositoryBuilder
	{
		private boolean expireOnShutdown = true;
		private int maxItemsToTrack = 100;

		public ExpiringFileRepositoryBuilder() {
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
		public ExpiringFileRepositoryBuilder timeBasedExpiration( long maxUnusedDuration, long maxAge ) {
			return expirationStrategy( timeBasedExpirationStrategy( maxUnusedDuration, maxAge ) );
		}
	}
}
