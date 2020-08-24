package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class SpringIntegrationFtpFolderResource extends SpringIntegrationFolderResource
{
	private final FolderDescriptor folderDescriptor;
	private final FtpRemoteFileTemplate remoteFileTemplate;

	SpringIntegrationFtpFolderResource( @NonNull FolderDescriptor folderDescriptor,
	                                    @NonNull FtpRemoteFileTemplate remoteFileTemplate ) {
		super( folderDescriptor, remoteFileTemplate );
		this.folderDescriptor = folderDescriptor;
		this.remoteFileTemplate = remoteFileTemplate;
	}

	@Override
	public Optional<FolderResource> getParentFolderResource() {
		return folderDescriptor.getParentFolderDescriptor()
		                       .map( fd -> new SpringIntegrationFtpFolderResource( fd, remoteFileTemplate ) );
	}

	@Override
	public FileRepositoryResource getResource( String relativePath ) {
		if ( relativePath.endsWith( "/" ) ) {
			return new SpringIntegrationFtpFolderResource( folderDescriptor.createFolderDescriptor( relativePath ), remoteFileTemplate );
		}

		FileDescriptor fileDescriptor = folderDescriptor.createFileDescriptor( relativePath );
		String actualPath = SpringIntegrationFtpFileResource.getPath( fileDescriptor );
		FTPFile ftpFile = remoteFileTemplate.exists( actualPath )
				? retrieveRemoteFile( actualPath )
				: null;

		return new SpringIntegrationFtpFileResource( fileDescriptor, ftpFile, remoteFileTemplate );
	}

	@Override
	@SuppressWarnings("Duplicates")
	public Collection<FileRepositoryResource> findResources( String pattern ) {
		if ( exists() ) {
			LOG.info( "Looking for resources with pattern '{}' in '{}'", pattern, getPath() );

			Set<FileRepositoryResource> resources = new LinkedHashSet<>();
			AntPathMatcher pathMatcher = new AntPathMatcher( "/" );
			String p = StringUtils.startsWith( pattern, "/" ) ? pattern.substring( 1 ) : pattern;
			boolean matchOnlyDirectories = StringUtils.endsWith( p, "/" );

			if ( matchOnlyDirectories ) {
				p = p.substring( 0, p.length() - 1 );
			}

			if ( !p.contains( "*" ) ) {
				return resolveExactPath( p );
			}

			if ( !p.contains( "**" ) ) {
				return resolvePatternForListing( p, matchOnlyDirectories );
			}

			BiPredicate<String, String> keyMatcher = ( candidateObjectName, antPattern ) -> {
				String path = antPattern.endsWith( "/" ) ? candidateObjectName : StringUtils.removeEnd( candidateObjectName, "/" );
				LOG.info( "[MATCHER] checking whether '{}' matches item '{}'", antPattern, path );
				if ( pathMatcher.match( antPattern, path ) ) {
					LOG.info( "[MATCHER] Matched! checking whether only directories should match: '{}', matches directory? '{}'", matchOnlyDirectories,
					          candidateObjectName.endsWith( "/" ) );
					return !matchOnlyDirectories || candidateObjectName.endsWith( "/" );
				}
				LOG.info( "[MATCHER] Not a match." );
				return false;
			};

			findResourcesWithMatchingKeys( keyMatcher, resources, getPath() + getValidPrefix( p ), StringUtils.prependIfMissing( p, "/" ) );

			return resources;
		}

		return Collections.emptyList();
	}

	private Collection<FileRepositoryResource> resolvePatternForListing( String p, boolean matchOnlyDirectories ) {
		LOG.info( "Got a pattern we can just pass to the client '{}'", p );

		if ( matchOnlyDirectories ) {
			return new ArrayList<>( retrieveFoldersForPath( p ) );
		}
		return Stream.concat( retrieveFoldersForPath( p ).stream(), retrieveFilesForPath( p ).stream() )
		             .collect( Collectors.toList() );
	}

	private Collection<FileRepositoryResource> resolveExactPath( String p ) {
		String pathToSearch = StringUtils.removeEnd( getPath(), "/" ) + "/" + StringUtils.removeStart( p, "/" );
		FTPFile ftpFile = retrieveRemoteFile( pathToSearch );

		if ( ftpFile == null ) {
			return Collections.emptyList();
		}

		if ( ftpFile.isDirectory() ) {
			return Collections.singletonList(
					new SpringIntegrationFtpFolderResource( FolderDescriptor.of( folderDescriptor.getRepositoryId(), pathToSearch ),
					                                        remoteFileTemplate ) );
		}
		FileDescriptor.of( folderDescriptor.getRepositoryId(), pathToSearch );
		return Collections.singletonList( createFileResource( ftpFile ) );
	}

	private FTPFile retrieveRemoteFile( String pathToSearch ) {
		return remoteFileTemplate.<FTPFile, FTPClient>executeWithClient( client -> retrieveRemoteFile( client, pathToSearch ) );
	}

	private FTPFile retrieveRemoteFile( FTPClient client, String path ) {
		try {
			return client.mlistFile( path );
		}
		catch ( IOException e ) {
			LOG.error( "Unexpected exception whilst retrieving file for path '{}'", path, e );
			return null;
		}
	}

	private void findResourcesWithMatchingKeys( BiPredicate<String, String> keyMatcher,
	                                            Set<FileRepositoryResource> resources,
	                                            String currentPath,
	                                            String keyPattern ) {
		if ( !keyPattern.endsWith( "/" ) ) {

			retrieveFilesForPath( currentPath )
					.stream()
					.filter( file -> {
						String filePath = SpringIntegrationFtpFileResource.getPath( file.getDescriptor() );
						LOG.info( "Checking whether file '{}' matches '{}'", filePath, keyPattern );
						return keyMatcher.test( filePath, keyPattern );
					} )
					.forEach( resources::add );
		}

		String remainingPatternPart = getRemainingPatternPart( keyPattern, currentPath );
		if ( remainingPatternPart != null && remainingPatternPart.startsWith( "**" ) ) {
			findAllResourcesThatMatches( keyMatcher, resources, currentPath, keyPattern );
		}
		else {
			findProgressivelyWithPartialMatch( keyMatcher, resources, currentPath, keyPattern );
		}
	}

	private void findAllResourcesThatMatches( BiPredicate<String, String> keyMatcher,
	                                          Set<FileRepositoryResource> resources,
	                                          String currentPath,
	                                          String keyPattern ) {
		LOG.info( "Recursively checking '{}' for matches to '{}'", currentPath, keyPattern );
		List<FolderResource> folders = retrieveFoldersForPath( currentPath );
		folders.stream()
		       .filter( f -> {
			       String folderPath = "/" + f.getDescriptor().getFolderId() + "/";
			       LOG.info( "Checking whether folder '{}' matches '{}'", folderPath, keyPattern );
			       return keyMatcher.test( folderPath, keyPattern );
		       } )
		       .forEach( resources::add );
		folders.forEach( f -> resources.addAll( f.findResources( keyPattern ) ) );
	}

	private void findProgressivelyWithPartialMatch( BiPredicate<String, String> keyMatcher,
	                                                Set<FileRepositoryResource> resources,
	                                                String currentPath,
	                                                String keyPattern ) {
		String remainingPatternPart = getRemainingPatternPart( keyPattern, currentPath );
		int nextSlash = StringUtils.indexOf( remainingPatternPart, "/" );
		if ( nextSlash != -1 ) {
			String nextPatternPath = StringUtils.substring( remainingPatternPart, nextSlash );
			String newCurrentPath = currentPath + "/" + StringUtils.removeEnd( remainingPatternPart, nextPatternPath );
			LOG.info( "Progressively: Checking whether '{}' matches '{}'", newCurrentPath, nextPatternPath );
			SpringIntegrationFtpFolderResource newFolderResource = new SpringIntegrationFtpFolderResource(
					FolderDescriptor.of( folderDescriptor.getRepositoryId(), newCurrentPath ), remoteFileTemplate );
			resources.addAll( newFolderResource.findResources( nextPatternPath ) );
		}
		else {
			String path = currentPath + keyPattern;
			LOG.info( "Progressively: looking into '{}'", path );
			resources.addAll( retrieveFilesForPath( path ) );
		}
	}

	@SuppressWarnings("Duplicates")
	private String getRemainingPatternPart( String keyPattern, String path ) {
		int numberOfSlashes = org.springframework.util.StringUtils.countOccurrencesOf( path, "/" );
		int indexOfNthSlash = getIndexOfNthOccurrence( keyPattern, numberOfSlashes );
		return indexOfNthSlash == -1 ? null : keyPattern.substring( indexOfNthSlash );
	}

	@SuppressWarnings("Duplicates")
	private int getIndexOfNthOccurrence( String str, int pos ) {
		int result = 0;
		String subStr = str;
		for ( int i = 0; i < pos; i++ ) {
			int nthOccurrence = subStr.indexOf( '/' );
			if ( nthOccurrence == -1 ) {
				return -1;
			}
			else {
				result += nthOccurrence + 1;
				subStr = subStr.substring( nthOccurrence + 1 );
			}
		}
		return result;
	}

	@SuppressWarnings("Duplicates")
	private String getValidPrefix( String keyPattern ) {
		int starIndex = keyPattern.indexOf( '*' );
		int markIndex = keyPattern.indexOf( '?' );
		int index = Math.min(
				starIndex == -1 ? keyPattern.length() : starIndex,
				markIndex == -1 ? keyPattern.length() : markIndex
		);
		String beforeIndex = keyPattern.substring( 0, index );
		return beforeIndex.contains( "/" ) ? beforeIndex.substring( 0, beforeIndex.lastIndexOf( '/' ) + 1 ) : "";
	}

	private List<FolderResource> retrieveFoldersForPath( String path ) {
		return remoteFileTemplate.<List<FolderResource>, FTPClient>executeWithClient( client -> retrieveFoldersForPath( client, path ) );
	}

	private List<FolderResource> retrieveFoldersForPath( FTPClient client, String path ) {
		LOG.info( "[FETCH] Retrieving folders for '{}'", path );
		FTPFile[] ftpFiles = null;
		try {
			String pathToSearch = StringUtils.removeEnd( getPath(), "/" ) + "/" + StringUtils.removeStart( path, "/" );
			ftpFiles = client.listDirectories( pathToSearch );
		}
		catch ( IOException e ) {
			LOG.error( "Unexpected error whilst listing directories for path '{}'. Falling back to no directories found.", path, e );
			ftpFiles = new FTPFile[0];
		}

		return Arrays.stream( ftpFiles )
		             .map( file -> new SpringIntegrationFtpFolderResource(
				             FolderDescriptor.of( folderDescriptor.getRepositoryId(), getPath() ).createFolderDescriptor( file.getName() ),
				             remoteFileTemplate ) )
		             .collect( Collectors.toList() );
	}

	private List<FileResource> retrieveFilesForPath( String path ) {
		return remoteFileTemplate.<List<FileResource>, FTPClient>executeWithClient( client -> retrieveFilesForPath( client, path ) );
	}

	private List<FileResource> retrieveFilesForPath( FTPClient client, String path ) {
		// this shit also matches folders, so we should exclude folders here
		LOG.info( "[FETCH] Retrieving files for '{}'", path );
		FTPFile[] ftpFiles = null;
		try {
			ftpFiles = client.listFiles( path );
		}
		catch ( IOException e ) {
			LOG.error( "Unexpected error whilst listing directories for path '{}'. Falling back to no directories found.", path, e );
			ftpFiles = new FTPFile[0];
		}

		return Arrays.stream( ftpFiles )
		             .filter( ftpFile -> !ftpFile.isDirectory() )
		             .map( this::createFileResource )
		             .collect( Collectors.toList() );
	}

	private SpringIntegrationFtpFileResource createFileResource( FTPFile file ) {
		String fileName = file.getName();
		String folderName = getPath();
		int lastIndexOfSlash = fileName.lastIndexOf( "/" );
		if ( lastIndexOfSlash != -1 ) {
			fileName = fileName.substring( lastIndexOfSlash + 1 );
			folderName = file.getName().replace( fileName, "" );
		}
		return new SpringIntegrationFtpFileResource( FileDescriptor.of( folderDescriptor.getRepositoryId(), folderName, fileName ),
		                                             file,
		                                             remoteFileTemplate );
	}

}
