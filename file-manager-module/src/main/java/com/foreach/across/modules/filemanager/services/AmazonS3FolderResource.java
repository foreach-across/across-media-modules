package com.foreach.across.modules.filemanager.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.foreach.across.modules.filemanager.business.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.AntPathMatcher;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiPredicate;

/**
 * Represents a folder resource on Amazon S3 storage.
 * A folder on S3 is not necessarily an object but might simply be a common path prefix between
 * several other objects. A separate folder object will only be created when calling {@link #create()},
 * in which case an empty object with trailing / will be added. For the consumer it should not really
 * make much of a difference if a folder is explicitly created this way or not.
 * <p/>
 * Likewise calling {@link #delete(boolean)} with {@code false} (not deleting child objects) will only
 * delete the folder object if it exists, but calls to {@link #exists()} will still return {@code true}.
 * <p/>
 * Borrows code from {@link org.springframework.cloud.aws.core.io.s3.PathMatchingSimpleStorageResourcePatternResolver}
 * for finding resources based on ANT patterns.
 *
 * @author Arne Vandamme
 * @since 1.4.0
 */
@RequiredArgsConstructor
class AmazonS3FolderResource implements FolderResource
{
	@Getter
	private final FolderDescriptor descriptor;

	private final AmazonS3 amazonS3;
	private final String bucketName;
	private final String objectName;
	private final TaskExecutor taskExecutor;

	@Override
	public Optional<FolderResource> getParentFolderResource() {
		return descriptor.getParentFolderDescriptor()
		                 .map( fd -> new AmazonS3FolderResource( fd, amazonS3, bucketName, extractParentObjectName(), taskExecutor ) );
	}

	private String extractParentObjectName() {
		Path parent = Paths.get( objectName ).getParent();
		return parent != null ? parent.toString() + "/" : "";
	}

	@Override
	public FileRepositoryResource getResource( @NonNull String relativePath ) {
		if ( relativePath.isEmpty() || "/".equals( relativePath ) ) {
			return this;
		}

		if ( relativePath.endsWith( "/" ) ) {
			FolderDescriptor folderDescriptor = descriptor.createFolderDescriptor( relativePath );
			String childPath = stripCurrentFolderId( folderDescriptor.getFolderId() );
			String childObjectName = Paths.get( objectName, childPath ).toString() + "/";
			return new AmazonS3FolderResource( folderDescriptor, amazonS3, bucketName, childObjectName, taskExecutor );
		}

		FileDescriptor fileDescriptor = descriptor.createFileDescriptor( relativePath );
		String childPath = stripCurrentFolderId( fileDescriptor.getFolderId() );
		String childObjectName = Paths.get( this.objectName, childPath, fileDescriptor.getFileId() ).toString();
		return new AmazonS3FileResource( fileDescriptor, amazonS3, bucketName, childObjectName, taskExecutor );
	}

	private String stripCurrentFolderId( String folderId ) {
		return StringUtils.defaultString( descriptor.getFolderId() != null ? StringUtils.removeStart( folderId, descriptor.getFolderId() ) : folderId );
	}

	@Override
	public Collection<FileRepositoryResource> findResources( String pattern ) {
		if ( exists() ) {
			Set<FileRepositoryResource> resources = new LinkedHashSet<>();
			AntPathMatcher pathMatcher = new AntPathMatcher( "/" );
			String p = StringUtils.startsWith( pattern, "/" ) ? pattern.substring( 1 ) : pattern;
			boolean matchOnlyDirectories = StringUtils.endsWith( p, "/" );

			if ( matchOnlyDirectories ) {
				p = p.substring( 0, p.length() - 1 );
			}

			BiPredicate<String, String> keyMatcher = ( candidateObjectName, antPattern ) -> {
				if ( pathMatcher.match( antPattern, antPattern.endsWith( "/" ) ? candidateObjectName : StringUtils.removeEnd( candidateObjectName, "/" ) ) ) {
					return !matchOnlyDirectories || candidateObjectName.endsWith( "/" );
				}
				return false;
			};

			findResourcesWithMatchingKeys( keyMatcher, resources, objectName + getValidPrefix( p ), objectName + p );

			return resources;
		}

		return Collections.emptyList();
	}

	private void findResourcesWithMatchingKeys( BiPredicate<String, String> keyMatcher,
	                                            Set<FileRepositoryResource> resources,
	                                            String prefix,
	                                            String keyPattern ) {
		String remainingPatternPart = getRemainingPatternPart( keyPattern, prefix );
		if ( remainingPatternPart != null && remainingPatternPart.startsWith( "**" ) ) {
			findAllResourcesThatMatches( keyMatcher, resources, prefix, keyPattern );
		}
		else {
			findProgressivelyWithPartialMatch( keyMatcher, resources, prefix, keyPattern );
		}
	}

	private void findAllResourcesThatMatches( BiPredicate<String, String> keyMatcher,
	                                          Set<FileRepositoryResource> resources,
	                                          String prefix,
	                                          String keyPattern ) {
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName( bucketName ).withPrefix( prefix );
		ObjectListing objectListing = null;

		do {
			try {
				if ( objectListing == null ) {
					objectListing = this.amazonS3.listObjects( listObjectsRequest );
				}
				else {
					objectListing = this.amazonS3.listNextBatchOfObjects( objectListing );
				}
				addResourcesFromObjectSummaries( keyMatcher, keyPattern, objectListing.getObjectSummaries(), resources );
				extractFolderResources( keyMatcher, keyPattern, prefix, objectListing, resources );
			}
			catch ( AmazonS3Exception e ) {
				if ( 301 != e.getStatusCode() ) {
					throw e;
				}
			}
		}
		while ( objectListing != null && objectListing.isTruncated() );
	}

	private void extractFolderResources( BiPredicate<String, String> keyMatcher,
	                                     String keyPattern, String prefix, ObjectListing objectListing, Set<FileRepositoryResource> resources ) {
		objectListing.getObjectSummaries().forEach( objectSummary -> {
			String resultObjectName = StringUtils.removeEnd( objectSummary.getKey(), "/" );

			int last = resultObjectName.lastIndexOf( '/' );

			if ( last > 0 ) {
				int delim = resultObjectName.indexOf( '/', prefix.length() + 1 );
				while ( delim != -1 && delim <= last ) {
					String partial = resultObjectName.substring( 0, delim + 1 );
					if ( partial.length() > 0 && keyMatcher.test( partial, keyPattern ) ) {
						resources.add( toFileRepositoryResource( partial, null ) );
					}
					delim = resultObjectName.indexOf( '/', delim + 1 );
				}
			}
		} );
	}

	private void findProgressivelyWithPartialMatch( BiPredicate<String, String> keyMatcher,
	                                                Set<FileRepositoryResource> resources,
	                                                String prefix,
	                                                String keyPattern ) {
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName( bucketName ).withDelimiter( "/" ).withPrefix( prefix );
		ObjectListing objectListing = null;

		do {
			if ( objectListing == null ) {
				objectListing = amazonS3.listObjects( listObjectsRequest );
			}
			else {
				objectListing = amazonS3.listNextBatchOfObjects( objectListing );
			}

			addResourcesFromObjectSummaries( keyMatcher, keyPattern, objectListing.getObjectSummaries(), resources );

			for ( String commonPrefix : objectListing.getCommonPrefixes() ) {
				if ( keyMatcher.test( commonPrefix, keyPattern ) ) {
					resources.add( toFileRepositoryResource( commonPrefix, null ) );
				}

				if ( isKeyPathMatchesPartially( keyMatcher, keyPattern, commonPrefix ) ) {
					findResourcesWithMatchingKeys( keyMatcher, resources, commonPrefix, keyPattern );
				}
			}
		}
		while ( objectListing.isTruncated() );
	}

	private String getRemainingPatternPart( String keyPattern, String path ) {
		int numberOfSlashes = org.springframework.util.StringUtils.countOccurrencesOf( path, "/" );
		int indexOfNthSlash = getIndexOfNthOccurrence( keyPattern, numberOfSlashes );
		return indexOfNthSlash == -1 ? null : keyPattern.substring( indexOfNthSlash );
	}

	private boolean isKeyPathMatchesPartially( BiPredicate<String, String> keyMatcher, String keyPattern, String keyPath ) {
		int numberOfSlashes = org.springframework.util.StringUtils.countOccurrencesOf( keyPath, "/" );
		int indexOfNthSlash = getIndexOfNthOccurrence( keyPattern, numberOfSlashes );
		if ( indexOfNthSlash != -1 ) {
			return keyMatcher.test( keyPath, keyPattern.substring( 0, indexOfNthSlash ) );
		}
		else {
			return false;
		}
	}

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

	private void addResourcesFromObjectSummaries( BiPredicate<String, String> keyMatcher,
	                                              String keyPattern,
	                                              List<S3ObjectSummary> objectSummaries,
	                                              Set<FileRepositoryResource> resources ) {
		objectSummaries.forEach( candidate -> {
			String candidateObjectName = candidate.getKey();
			if ( !candidateObjectName.equals( objectName ) && keyMatcher.test( candidateObjectName, keyPattern ) ) {
				resources.add( toFileRepositoryResource( candidateObjectName, candidate ) );
			}
		} );
	}

	private FileRepositoryResource toFileRepositoryResource( String childObjectName, S3ObjectSummary childObjectSummary ) {
		String childPath = StringUtils.removeStart( childObjectName, objectName );
		if ( childObjectName.endsWith( "/" ) ) {
			return new AmazonS3FolderResource( descriptor.createFolderDescriptor( childPath ), amazonS3, bucketName, childObjectName, taskExecutor );
		}

		AmazonS3FileResource fileResource = new AmazonS3FileResource( descriptor.createFileDescriptor( childPath ), amazonS3, bucketName,
		                                                              childObjectName, taskExecutor );
		if ( childObjectSummary != null ) {
			fileResource.loadMetadata( childObjectSummary );
		}

		return fileResource;
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

	@Override
	public boolean delete( boolean deleteChildren ) {
		boolean deleted = false;

		if ( deleteChildren ) {
			deleted = deleteChildren();
		}

		if ( isNotRootFolder() ) {
			amazonS3.deleteObject( bucketName, objectName );
			deleted = true;
		}

		return deleted;
	}

	@Override
	public boolean deleteChildren() {
		Collection<FileRepositoryResource> resources = findResources( "*" );

		if ( !resources.isEmpty() ) {
			try {
				resources.forEach( r -> {
					if ( r instanceof FileResource ) {
						( (FileResource) r ).delete();
					}
					else {
						( (FolderResource) r ).delete( true );
					}
				} );
			}
			catch ( AmazonS3Exception ignore ) {
				return false;
			}
			return true;
		}

		return false;
	}

	@Override
	public boolean create() {
		if ( isNotRootFolder() && !amazonS3.doesObjectExist( bucketName, objectName ) ) {
			// explicitly create a folder object
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength( 0 );
			InputStream emptyContent = new ByteArrayInputStream( new byte[0] );
			PutObjectRequest putObjectRequest = new PutObjectRequest( bucketName, objectName, emptyContent, metadata );
			amazonS3.putObject( putObjectRequest );

			return true;
		}

		return false;
	}

	@Override
	public boolean exists() {
		// a folder concept in S3 always exists as it can contain resources
		return true;
	}

	private boolean isNotRootFolder() {
		return !"".equals( objectName );
	}

	@Override
	public String toString() {
		return "axfs [" + descriptor.toString() + "] -> Amazon s3 resource [bucket='" + this.bucketName + "' and object='" + this.objectName + "']";
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
