package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileRepositoryResource;
import com.foreach.across.modules.filemanager.business.FolderDescriptor;
import com.foreach.across.modules.filemanager.business.FolderResource;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Consumer;

/**
 * Represents a single folder (directory) on a {@link LocalFileRepository}.
 *
 * @author Arne Vandamme
 * @since 1.4.0
 */
@RequiredArgsConstructor
class LocalFolderResource implements FolderResource
{
	private final LocalFileRepository fileRepository;

	@Getter
	private final FolderDescriptor descriptor;

	private final File directory;

	@Override
	public Optional<FolderResource> getParentFolderResource() {
		return descriptor.getParentFolderDescriptor().map( fileRepository::getFolderResource );
	}

	@Override
	public FileRepositoryResource getResource( @NonNull String relativePath ) {
		if ( relativePath.isEmpty() || "/".equals( relativePath ) ) {
			return this;
		}

		String path = relativePath.startsWith( "/" ) ? relativePath : "/" + relativePath;

		if ( relativePath.endsWith( "/" ) ) {
			FolderDescriptor folderDescriptor = FolderDescriptor.of( descriptor.getRepositoryId() + ":" + descriptor.getFolderId() + path );
			String childPath = StringUtils.removeStart( folderDescriptor.getFolderId(), descriptor.getFolderId() );
			return new LocalFolderResource( fileRepository, folderDescriptor, new File( directory, childPath ) );
		}

		FileDescriptor fileDescriptor = FileDescriptor.of( descriptor.getRepositoryId() + ":" + descriptor.getFolderId() + path );
		String childPath = StringUtils.removeStart( fileDescriptor.getFolderId(), descriptor.getFolderId() );
		return new LocalFileResource( fileRepository, fileDescriptor, new File( directory, childPath + "/" + fileDescriptor.getFileId() ) );
	}

	@Override
	@SneakyThrows
	public Collection<FileRepositoryResource> findResources( @NonNull String pattern ) {
		if ( exists() ) {
			List<FileRepositoryResource> resources = new ArrayList<>();
			AntPathMatcher pathMatcher = new AntPathMatcher( "/" );
			String p = StringUtils.startsWith( pattern, "/" ) ? pattern : "/" + pattern;
			if ( pathMatcher.isPattern( p ) ) {
				boolean matchOnlyDirectories = StringUtils.endsWith( p, "/" );

				if ( matchOnlyDirectories ) {
					p = p.substring( 0, p.length() - 1 );
				}

				boolean shouldRecurse = !"/*".equalsIgnoreCase( p );

				Path rootPath = directory.toPath();
				String pathPrefix = StringUtils.replace( rootPath.toAbsolutePath().toString(), "\\", "/" );

				final String antPattern = p;

				Consumer<Path> processCandidate = candidate -> {
					String candidatePath = StringUtils.replace( candidate.toAbsolutePath().toString(), "\\", "/" );
					if ( candidatePath.length() > pathPrefix.length() ) {
						String pathToMatch = StringUtils.substring( candidatePath, pathPrefix.length() );
						if ( !shouldRecurse || pathMatcher.match( antPattern, pathToMatch ) ) {
							File file = candidate.toFile();
							if ( !matchOnlyDirectories || file.isDirectory() ) {
								resources.add( toFileRepositoryResource( file, pathToMatch ) );
							}
						}
					}
				};

				if ( shouldRecurse ) {
					Files.walkFileTree( rootPath, new SimpleFileVisitor<Path>()
					{
						@Override
						public FileVisitResult preVisitDirectory( Path candidate, BasicFileAttributes attrs ) {
							processCandidate.accept( candidate );
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFile( Path candidate, BasicFileAttributes attrs ) {
							processCandidate.accept( candidate );
							return FileVisitResult.CONTINUE;
						}
					} );
				}
				else {
					Files.list( directory.toPath() ).forEach( processCandidate );
				}

				return resources;
			}
		}

		return Collections.emptyList();
	}

	private FileRepositoryResource toFileRepositoryResource( File candidate, String childPath ) {
		if ( candidate.isDirectory() ) {
			return new LocalFolderResource(
					fileRepository,
					FolderDescriptor.of( descriptor.getRepositoryId(), descriptor.getFolderId() + childPath ),
					candidate
			);
		}

		return new LocalFileResource( fileRepository, FileDescriptor.of( descriptor.getRepositoryId() + ":" + descriptor.getFolderId() + childPath ),
		                              candidate );
	}

	@Override
	public boolean delete( boolean deleteChildren ) {
		if ( exists() ) {
			if ( deleteChildren ) {
				try {
					FileUtils.deleteDirectory( directory );
					return true;
				}
				catch ( IOException ioe ) {
					return false;
				}
			}
			return directory.delete();
		}
		return false;
	}

	@Override
	public boolean deleteChildren() {
		if ( exists() ) {
			try {
				FileUtils.cleanDirectory( directory );
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
				FileUtils.forceMkdir( directory );
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
		return directory.exists() && directory.isDirectory();
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
