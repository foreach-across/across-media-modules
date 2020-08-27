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
	public boolean exists() {
		return retrieveRemoteFile( getPath() ) != null;
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

			if ( !p.contains( "*" ) && !p.contains( "?" ) ) {
				return resolveExactPath( p );
			}

			if ( !p.contains( "**" ) && !p.contains( "?" ) ) {
				return resolvePatternForListing( p, matchOnlyDirectories );
			}

			BiPredicate<String, String> keyMatcher = ( candidateObjectName, antPattern ) -> {
				String patternToMatch = !StringUtils.isBlank( antPattern ) ? StringUtils.prependIfMissing( antPattern, "/" ) : antPattern;
				String path = patternToMatch.endsWith( "/" ) ? candidateObjectName : StringUtils.removeEnd( candidateObjectName, "/" );
				LOG.info( "[MATCHER] checking whether pattern '{}' matches item '{}'", patternToMatch, path );
				if ( pathMatcher.match( patternToMatch, path ) ) {
					LOG.info( "[MATCHER] Matched! checking whether only directories should match: '{}', matches directory? '{}'", matchOnlyDirectories,
					          candidateObjectName.endsWith( "/" ) );
					return !matchOnlyDirectories || candidateObjectName.endsWith( "/" );
				}
				LOG.info( "[MATCHER] Not a match." );
				return false;
			};

			findResourcesWithMatchingKeys( keyMatcher, resources, getPath() + getValidPrefix( p ), StringUtils.prependIfMissing( p, "/" ),
			                               matchOnlyDirectories );

			return resources;
		}

		return Collections.emptyList();
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

	private Collection<FileRepositoryResource> resolvePatternForListing( String p, boolean matchOnlyDirectories ) {
		LOG.info( "Got a pattern we can just pass to the client '{}'", p );
		if ( p.endsWith( "*" ) ) {
			String withoutEndListing = StringUtils.removeEnd( p, "*" );
			if ( !withoutEndListing.isEmpty() ) {
				List<FolderResource> folders;
				if ( !withoutEndListing.contains( "*" ) ) {
					folders = Collections.singletonList( getFolderResource( withoutEndListing ) );
				}
				else {
					String pathToSearch = StringUtils.removeEnd( getPath(), "/" ) + "/" + StringUtils.removeStart( withoutEndListing, "/" );
					folders = retrieveFoldersForPath( pathToSearch );
				}

				return folders.stream()
				              .map( f -> f.findResources( matchOnlyDirectories ? "*/" : "*" ) )
				              .flatMap( Collection::stream )
				              .collect( Collectors.toList() );
			}
		}

		String pathToSearch = p.length() > 1
				? StringUtils.removeEnd( getPath(), "/" ) + "/" + StringUtils.removeStart( p, "/" )
				: getPath();
		if ( matchOnlyDirectories ) {
			return new ArrayList<>( retrieveFoldersForPath( pathToSearch ) );
		}

		return Stream.concat( retrieveFoldersForPath( pathToSearch ).stream(), retrieveFilesForPath( pathToSearch ).stream() )
		             .collect( Collectors.toList() );
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
	                                            String keyPattern, boolean matchOnlyDirectories ) {
		if ( !keyPattern.endsWith( "/" ) ) {
			retrieveFilesForPath( currentPath )
					.stream()
					.filter( file -> {
						String path = SpringIntegrationFtpFileResource.getPath( file.getDescriptor() );
						LOG.info( "Checking whether file '{}' matches '{}'", path, keyPattern );
						return keyMatcher.test( path, keyPattern );
					} )
					.forEach( resources::add );
		}

		String pathToLookFor = currentPath;
		String patternBasedOnPath = keyPattern;
		if ( !keyPattern.startsWith( "**" ) && !keyPattern.startsWith( "?" ) ) {
			String nextValidPrefix = getValidPrefix( keyPattern );
			String remainingKeyPattern = StringUtils.removeStart( keyPattern, nextValidPrefix );
			pathToLookFor = currentPath + StringUtils.prependIfMissing( nextValidPrefix, "/" );
			pathToLookFor = pathToLookFor.replaceAll( "//", "/" );
			patternBasedOnPath = remainingKeyPattern;
		}
		String newKeyPattern = patternBasedOnPath;

		List<FolderResource> folderResources = retrieveFoldersForPath( pathToLookFor );
		folderResources.stream()
		               .filter( folder -> {
			               String path = StringUtils.appendIfMissing( SpringIntegrationFtpFolderResource.getPath( folder.getDescriptor() ), "/" );
			               LOG.info( "Checking whether folder '{}' matches '{}'", path, newKeyPattern );
			               return keyMatcher.test( path, newKeyPattern );
		               } )
		               .forEach( resources::add );

		folderResources
				.forEach(
						f -> {
							String recursivePattern = matchOnlyDirectories ? StringUtils.appendIfMissing( newKeyPattern, "/" ) : newKeyPattern;
							resources.addAll( f.findResources( recursivePattern ) );
						}
				);

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
			ftpFiles = client.listDirectories( path );
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
