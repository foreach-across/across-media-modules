package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.*;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.util.AntPathMatcher;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Steven Gentens
 * @since 2.3.0
 */
@Slf4j
public class SpringIntegrationSftpFolderResource extends SpringIntegrationFolderResource
{
	private final FolderDescriptor folderDescriptor;
	private final SftpRemoteFileTemplate remoteFileTemplate;

	SpringIntegrationSftpFolderResource( @NonNull FolderDescriptor folderDescriptor,
	                                     @NonNull SftpRemoteFileTemplate remoteFileTemplate ) {
		super( folderDescriptor, remoteFileTemplate );
		this.folderDescriptor = folderDescriptor;
		this.remoteFileTemplate = remoteFileTemplate;
	}

	@Override
	public boolean exists() {
		return retrieveRemoteFile( getPath() ).exists();
	}

	protected boolean exists( ChannelSftp client ) {
		return retrieveRemoteFile( client, getPath() ) != null;
	}

	@Override
	public Optional<FolderResource> getParentFolderResource() {
		return folderDescriptor.getParentFolderDescriptor()
		                       .map( fd -> new SpringIntegrationSftpFolderResource( fd, remoteFileTemplate ) );
	}

	@Override
	public FileRepositoryResource getResource( @NonNull String relativePath ) {
		if ( relativePath.isEmpty() || "/".equals( relativePath ) ) {
			return this;
		}

		if ( relativePath.endsWith( "/" ) ) {
			return new SpringIntegrationSftpFolderResource( folderDescriptor.createFolderDescriptor( relativePath ), remoteFileTemplate );
		}

		FileDescriptor fileDescriptor = folderDescriptor.createFileDescriptor( relativePath );
		String actualPath = SpringIntegrationSftpFileResource.getPath( fileDescriptor );
		SFTPFile ftpFile = remoteFileTemplate.exists( actualPath )
				? retrieveRemoteFile( actualPath )
				: null;

		return new SpringIntegrationSftpFileResource( fileDescriptor, ftpFile, remoteFileTemplate );
	}

	@Override
	@SuppressWarnings("Duplicates")
	public Collection<FileRepositoryResource> findResources( @NonNull String pattern ) {
		try (Session session = remoteFileTemplate.getSession();) {
			ChannelSftp client = (ChannelSftp) session.getClientInstance();
			return findResources( pattern, client );
		}
	}

	@SuppressWarnings("Duplicates")
	protected Collection<FileRepositoryResource> findResources( @NonNull String pattern, ChannelSftp client ) {
		if ( exists( client ) ) {
			Set<FileRepositoryResource> resources = new LinkedHashSet<>();
			AntPathMatcher pathMatcher = new AntPathMatcher( "/" );
			String p = StringUtils.startsWith( pattern, "/" ) ? pattern.substring( 1 ) : pattern;
			boolean matchOnlyDirectories = StringUtils.endsWith( p, "/" );

			if ( matchOnlyDirectories ) {
				p = p.substring( 0, p.length() - 1 );
			}

			if ( !p.contains( "*" ) && !p.contains( "?" ) ) {
				return resolveExactPath( p, client );
			}

			BiPredicate<String, String> keyMatcher = ( candidateObjectName, antPattern ) -> {
				String patternToMatch = !StringUtils.isBlank( antPattern ) ? StringUtils.prependIfMissing( antPattern, "/" ) : antPattern;
				String path = patternToMatch.endsWith( "/" ) ? candidateObjectName : StringUtils.removeEnd( candidateObjectName, "/" );
				if ( pathMatcher.match( patternToMatch, path ) ) {
					return !matchOnlyDirectories || candidateObjectName.endsWith( "/" );
				}
				return false;
			};

			if ( !p.contains( "**" ) && !p.contains( "?" ) ) {
				return resolvePatternForListing( p, matchOnlyDirectories, keyMatcher, client );
			}

			findResourcesWithMatchingKeys( keyMatcher, resources, getPath() + getValidPrefix( p ), StringUtils.prependIfMissing( p, "/" ),
			                               matchOnlyDirectories, client );

			return resources;
		}

		return Collections.emptyList();
	}

	private Collection<FileRepositoryResource> resolveExactPath( String p, ChannelSftp client ) {
		String pathToSearch = StringUtils.removeEnd( getPath(), "/" ) + "/" + StringUtils.removeStart( p, "/" );
		SFTPFile ftpFile = retrieveRemoteFile( client, pathToSearch );

		if ( ftpFile == null ) {
			return Collections.emptyList();
		}

		if ( ftpFile.isDirectory() ) {
			return Collections.singletonList(
					new SpringIntegrationSftpFolderResource( FolderDescriptor.of( folderDescriptor.getRepositoryId(), pathToSearch ),
					                                         remoteFileTemplate ) );
		}
		return Collections.singletonList( createFileResource( ftpFile, StringUtils.removeStart( pathToSearch, "/" ) ) );
	}

	@SuppressWarnings("Duplicates")
	private Collection<FileRepositoryResource> resolvePatternForListing( String p,
	                                                                     boolean matchOnlyDirectories,
	                                                                     BiPredicate<String, String> keyMatcher,
	                                                                     ChannelSftp client ) {
		String validPrefix = getValidPrefix( p );
		String remainingPattern = StringUtils.removeStart( p, validPrefix );

		String pathToSearch =
				StringUtils.removeEnd( getPath(), "/" ) + "/" + StringUtils.removeStart( validPrefix, "/" );
		// not yet the final part
		if ( remainingPattern.contains( "/" ) ) {
			int nextSlash = StringUtils.indexOf( remainingPattern, "/" );
			String nextPart = StringUtils.substring( remainingPattern, 0, nextSlash + 1 );
			String keyPattern = StringUtils.removeEnd( getPath(), "/" ) + "/" + StringUtils.removeStart( nextPart, "/" );
			String withoutNextPart = StringUtils.removeStart( remainingPattern, nextPart );
			return retrieveFoldersForPath( client, pathToSearch )
					.stream()
					.filter( f -> {
						String pathToTest = StringUtils.appendIfMissing( f.getPath(), "/" );
						return keyMatcher.test( pathToTest, keyPattern );
					} )
					.map( f -> {
						String pathToFind = matchOnlyDirectories ? StringUtils.appendIfMissing( withoutNextPart, "/" ) : withoutNextPart;
						return f.findResources( pathToFind, client );
					} )
					.flatMap( Collection::stream )
					.collect( Collectors.toSet() );
		}

		SpringIntegrationSftpFolderResource baseResource = validPrefix.isEmpty() ? this : (SpringIntegrationSftpFolderResource) getFolderResource(
				validPrefix );

		String keyPattern = StringUtils.removeEnd( baseResource.getPath(), "/" ) + "/" + StringUtils.removeStart( remainingPattern, "/" );
		if ( matchOnlyDirectories ) {
			return baseResource.retrieveFoldersForPath( client, pathToSearch )
			                   .stream()
			                   .filter( f -> {
				                   String pathToTest = StringUtils.appendIfMissing( f.getPath(), "/" );
				                   return keyMatcher.test( pathToTest, keyPattern );
			                   } )
			                   .collect( Collectors.toSet() );
		}

		return Stream.concat( baseResource.retrieveFoldersForPath( client, pathToSearch ).stream(),
		                      baseResource.retrieveFilesForPath( client, pathToSearch ).stream() )
		             .filter( f -> {
			             if ( f instanceof FolderResource ) {
				             String folderPath = SpringIntegrationFolderResource.getPath( ( (FolderResource) f ).getDescriptor() );
				             String pathToTest = StringUtils.appendIfMissing( folderPath, "/" );
				             return keyMatcher.test( pathToTest, keyPattern );
			             }
			             String pathToTest = SpringIntegrationFileResource.getPath( ( (FileResource) f ).getDescriptor() );
			             return keyMatcher.test( pathToTest, keyPattern );
		             } ).collect( Collectors.toSet() );
	}

	private SFTPFile retrieveRemoteFile( String pathToSearch ) {
		return remoteFileTemplate.<SFTPFile, ChannelSftp>executeWithClient( client -> retrieveRemoteFile( client, pathToSearch ) );
	}

	private SFTPFile retrieveRemoteFile( ChannelSftp client, String path ) {
		return new SFTPFile( remoteFileTemplate, path );
	}

	private void findResourcesWithMatchingKeys( BiPredicate<String, String> keyMatcher,
	                                            Set<FileRepositoryResource> resources,
	                                            String currentPath,
	                                            String keyPattern, boolean matchOnlyDirectories, ChannelSftp client ) {
		if ( !keyPattern.endsWith( "/" ) ) {
			retrieveFilesForPath( client, currentPath )
					.stream()
					.filter( file -> {
						String path = SpringIntegrationFtpFileResource.getPath( file.getDescriptor() );
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

		if ( !newKeyPattern.contains( "/" ) && !matchOnlyDirectories && newKeyPattern.contains( "?" ) ) {
			String matchingPattern = pathToLookFor + StringUtils.removeStart( newKeyPattern, "/" );
			List<SpringIntegrationSftpFileResource> filesForPath = retrieveFilesForPath( client, pathToLookFor );
			filesForPath.stream()
			            .filter( file -> {
				            String path = SpringIntegrationFileResource.getPath( file.getDescriptor() );
				            return keyMatcher.test( path, matchingPattern );
			            } )
			            .forEach( resources::add );
		}

		List<SpringIntegrationSftpFolderResource> folderResources = retrieveFoldersForPath( client, pathToLookFor );
		folderResources.stream()
		               .filter( folder -> {
			               String path = StringUtils.appendIfMissing( SpringIntegrationSftpFolderResource.getPath( folder.getDescriptor() ), "/" );
			               return keyMatcher.test( path, newKeyPattern );
		               } )
		               .forEach( resources::add );

		folderResources
				.forEach(
						f -> {
							String recursivePattern = matchOnlyDirectories ? StringUtils.appendIfMissing( newKeyPattern, "/" ) : newKeyPattern;
							resources.addAll( f.findResources( recursivePattern, client ) );
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

	@SuppressWarnings("unchecked")
	private List<SpringIntegrationSftpFolderResource> retrieveFoldersForPath( ChannelSftp client, String path ) {
		List<String> ftpFolderNames = new ArrayList<>();
		try {
			Vector<ChannelSftp.LsEntry> files = client.ls( path );
			for ( ChannelSftp.LsEntry entry : files ) {
				if ( !entry.getFilename().equals( "." ) && !entry.getFilename().equals( ".." ) && entry.getAttrs().isDir() ) {
					ftpFolderNames.add( entry.getFilename() );
				}
			}
		}
		catch ( SftpException e ) {
			LOG.error( "Unexpected error whilst listing directories for path '{}'. Falling back to no directories found.", path, e );
		}

		return ftpFolderNames.stream()
		                     .map( fileName -> new SpringIntegrationSftpFolderResource(
				                     FolderDescriptor.of( folderDescriptor.getRepositoryId(), getPath() ).createFolderDescriptor( fileName ),
				                     remoteFileTemplate ) )
		                     .collect( Collectors.toList() );
	}

	@SuppressWarnings("unchecked")
	private List<SpringIntegrationSftpFileResource> retrieveFilesForPath( ChannelSftp client, String path ) {
		List<String> ftpFileNames = new ArrayList<>();

		try {
			Vector<ChannelSftp.LsEntry> files = client.ls( path );
			for ( ChannelSftp.LsEntry entry : files ) {
				if ( !entry.getAttrs().isDir() ) {
					ftpFileNames.add( entry.getFilename() );
				}
			}
		}
		catch ( SftpException e ) {
			LOG.error( "Unexpected error whilst listing files for path '{}'. Falling back to no directories found.", path, e );
		}

		return ftpFileNames.stream()
		                   .map( fileName -> createFileResource( new SFTPFile( remoteFileTemplate, path + "/" + fileName ) ) ) // iffy full path creation maybe
		                   .collect( Collectors.toList() );
	}

	private SpringIntegrationSftpFileResource createFileResource( SFTPFile file ) {
		return createFileResource( file, file.getName() );
	}

	private SpringIntegrationSftpFileResource createFileResource( SFTPFile file, String fullPath ) {
		String fileName = fullPath;
		String folderName = getPath();
		int lastIndexOfSlash = fileName.lastIndexOf( "/" );
		if ( lastIndexOfSlash != -1 ) {
			fileName = fileName.substring( lastIndexOfSlash + 1 );
			folderName = fullPath.replace( fileName, "" );
		}
		return new SpringIntegrationSftpFileResource( FileDescriptor.of( folderDescriptor.getRepositoryId(), folderName, fileName ),
		                                              file,
		                                              remoteFileTemplate );
	}

}
