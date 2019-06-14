package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileRepositoryResource;
import com.foreach.across.modules.filemanager.business.FolderDescriptor;
import com.foreach.across.modules.filemanager.business.FolderResource;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Represents a single folder (directory) on a {@link LocalFileRepository}.
 *
 * @author Arne Vandamme
 * @since 1.4.0
 */
@Slf4j
@RequiredArgsConstructor
class LocalFolderResource implements FolderResource
{
	@Getter
	private final FolderDescriptor descriptor;

	private final Path directory;

	@Override
	public Optional<FolderResource> getParentFolderResource() {
		return descriptor.getParentFolderDescriptor().map( fd -> new LocalFolderResource( fd, directory.getParent() ) );
	}

	@Override
	public FileRepositoryResource getResource( @NonNull String relativePath ) {
		if ( relativePath.isEmpty() || "/".equals( relativePath ) ) {
			return this;
		}

		if ( relativePath.endsWith( "/" ) ) {
			FolderDescriptor folderDescriptor = descriptor.createFolderDescriptor( relativePath );
			String childPath = stripCurrentFolderId( folderDescriptor.getFolderId() );
			return new LocalFolderResource( folderDescriptor, Paths.get( directory.toString(), childPath ) );
		}

		FileDescriptor fileDescriptor = descriptor.createFileDescriptor( relativePath );
		String childPath = stripCurrentFolderId( fileDescriptor.getFolderId() );
		return new LocalFileResource( fileDescriptor, Paths.get( directory.toString(), childPath, fileDescriptor.getFileId() ) );
	}

	private String stripCurrentFolderId( String folderId ) {
		return StringUtils.defaultString( descriptor.getFolderId() != null ? StringUtils.removeStart( folderId, descriptor.getFolderId() ) : folderId );
	}

	@Override
	@SneakyThrows
	@SuppressWarnings("squid:S3776")
	public Collection<FileRepositoryResource> findResources( @NonNull String pattern ) {
		if ( exists() ) {
			List<FileRepositoryResource> resources = new ArrayList<>();
			AntPathMatcher pathMatcher = new AntPathMatcher( "/" );
			String p = StringUtils.startsWith( pattern, "/" ) ? pattern : "/" + pattern;
			boolean usePathMatcher = pathMatcher.isPattern( p );
			boolean matchOnlyDirectories = StringUtils.endsWith( p, "/" );

			if ( matchOnlyDirectories ) {
				p = p.substring( 0, p.length() - 1 );
			}

			boolean shouldRecurse = !"/*".equalsIgnoreCase( p );

			Path rootPath = directory;
			String pathPrefix = StringUtils.replace( rootPath.toAbsolutePath().toString(), "\\", "/" );

			final String antPattern = p;

			Consumer<Path> processCandidate = candidate -> {
				String candidatePath = StringUtils.replace( candidate.toAbsolutePath().toString(), "\\", "/" );
				if ( candidatePath.length() > pathPrefix.length() ) {
					String pathToMatch = StringUtils.substring( candidatePath, pathPrefix.length() );
					if ( ( !shouldRecurse || ( usePathMatcher ? pathMatcher.match( antPattern, pathToMatch ) : StringUtils.equals( pathToMatch, antPattern ) ) )
							&& ( !matchOnlyDirectories || candidate.toFile().isDirectory() ) ) {
						resources.add( toFileRepositoryResource( candidate, pathToMatch ) );
					}
				}
			};

			if ( shouldRecurse ) {
				Files.walkFileTree( rootPath, new SimpleFileVisitor<Path>()
				{
					@Override
					public FileVisitResult preVisitDirectory( Path candidate, BasicFileAttributes attrs ) {
						return visitFile( candidate, attrs );
					}

					@Override
					public FileVisitResult visitFile( Path candidate, BasicFileAttributes attrs ) {
						processCandidate.accept( candidate );
						return FileVisitResult.CONTINUE;
					}
				} );
			}
			else {
				try (Stream<Path> list = Files.list( directory )) {
					list.forEach( processCandidate );
				}
			}

			return resources;
		}

		return Collections.emptyList();
	}

	private FileRepositoryResource toFileRepositoryResource( Path candidate, String childPath ) {
		if ( candidate.toFile().isDirectory() ) {
			return new LocalFolderResource( descriptor.createFolderDescriptor( childPath ), candidate );
		}

		return new LocalFileResource( descriptor.createFileDescriptor( childPath ), candidate );
	}

	@Override
	@SneakyThrows
	public boolean delete( boolean deleteChildren ) {
		if ( exists() ) {
			if ( deleteChildren ) {
				try {
					FileUtils.deleteDirectory( directory.toFile() );
					return true;
				}
				catch ( IOException ioe ) {
					LOG.trace( "Exception deleting children of {}", directory, ioe );
					return false;
				}
			}
			try {
				Files.delete( directory );
				return true;
			}
			catch ( IOException ioe ) {
				LOG.trace( "Exception deleting directory {}", directory, ioe );
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean deleteChildren() {
		if ( exists() ) {
			try {
				FileUtils.cleanDirectory( directory.toFile() );
			}
			catch ( IOException ioe ) {
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean create() {
		try {
			if ( !exists() ) {
				Files.createDirectories( directory );
				return true;
			}
			return false;
		}
		catch ( IOException ioe ) {
			return false;
		}
	}

	@Override
	public boolean exists() {
		File file = directory.toFile();
		return file.exists() && file.isDirectory();
	}

	@Override
	public String toString() {
		return "axfs [" + descriptor.toString() + "] -> dir [" + directory.toString() + "]";
	}

	@Override
	public boolean equals( Object obj ) {
		return obj == this || ( obj instanceof FolderResource && descriptor.equals( ( (FolderResource) obj ).getDescriptor() ) );
	}

	@Override
	public int hashCode() {
		return descriptor.hashCode();
	}
}
