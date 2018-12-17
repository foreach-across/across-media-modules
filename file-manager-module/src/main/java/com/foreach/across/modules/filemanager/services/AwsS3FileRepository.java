/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.modules.filemanager.services;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class AwsS3FileRepository implements FileRepository
{

	public static final String DEFAULT_REGION = "eu-central-1";

	private final String repositoryId;
	private final String bucketName; //bucketName
	private final AmazonS3 amazonS3Client;
	private final Optional<PathGenerator> pathGenerator;
	private final FileManager fileManager;

	public AwsS3FileRepository( String bucketName, String accessKey, String accessSecret, FileManager fileManager ) {
		this( bucketName, accessKey, accessSecret, fileManager, DEFAULT_REGION, null );
	}

	public AwsS3FileRepository( String bucketName, String accessKey, String accessSecret, FileManager fileManager, String region ) {
		this( bucketName, accessKey, accessSecret, fileManager, region, null );
	}

	public AwsS3FileRepository( String bucketName,
	                            String accessKey,
	                            String accessSecret,
	                            FileManager fileManager,
	                            String region,
	                            PathGenerator pathGenerator ) {
		this( bucketName, bucketName, accessKey, accessSecret, fileManager, region, pathGenerator );
	}

	public AwsS3FileRepository( String repositoryId,
	                            String bucketName,
	                            String accessKey,
	                            String accessSecret,
	                            FileManager fileManager,
	                            String region,
	                            PathGenerator pathGenerator ) {
		this.repositoryId = repositoryId;
		this.bucketName = bucketName;
		this.pathGenerator = Optional.ofNullable( pathGenerator );
		this.fileManager = fileManager;
		amazonS3Client = AmazonS3ClientBuilder.standard()
		                                      .withRegion( StringUtils.isNoneBlank( region ) ? region : DEFAULT_REGION )
		                                      .withCredentials( new AWSStaticCredentialsProvider( new BasicAWSCredentials( accessKey, accessSecret ) ) )
		                                      .build();
	}

	@Override
	public String getRepositoryId() {
		return repositoryId;
	}

	@Override
	public FileDescriptor createFile() {
		FileDescriptor descriptor = buildNewDescriptor( null );
		try {
			if ( descriptor.getFolderId() != null ) {
				createFolder( descriptor.getFolderId() );
			}
			//create empty file
			amazonS3Client.putObject( bucketName, buildAwsPath( descriptor ), "" );
			getAsFile( descriptor );
		}
		catch ( AmazonServiceException a ) {
			LOG.error( "Unable to create new file on Amazon", a );
			throw new FileStorageException( a );
		}

		return descriptor;
	}

	@Override
	public FileDescriptor moveInto( File file ) {
		FileDescriptor descriptor = save( file );
		if ( !file.delete() ) {
			LOG.warn( "File {} was copied into the AmazonS3FileRepository but could not be deleted", file );
		}
		return descriptor;
	}

	@Override
	public FileDescriptor save( File file ) {
		FileDescriptor descriptor = buildNewDescriptor( file.getName() );
		try {
			if ( descriptor.getFolderId() != null ) {
				createFolder( descriptor.getFolderId() );
			}
			amazonS3Client.putObject( bucketName, buildAwsPath( descriptor ), file );
		}
		catch ( AmazonServiceException e ) {
			LOG.error( "Unable to save file on Amazon", e );
			throw new FileStorageException( e );
		}
		return descriptor;
	}

	@Override
	public FileDescriptor save( InputStream inputStream ) {
		FileDescriptor descriptor = buildNewDescriptor( null );
		return save( descriptor, inputStream, true );
	}

	public FileDescriptor save( InputStream inputStream, FileDescriptor descriptor ) {
		return save( descriptor, inputStream, true );
	}

	@Override
	public FileDescriptor save( FileDescriptor target, InputStream inputStream, boolean overwriteExisting ) {
		if ( !StringUtils.equals( repositoryId, target.getRepositoryId() ) ) {
			throw new IllegalArgumentException(
					"Invalid file descriptor. File repository " + target.getRepositoryId() +
							" can not persist a file for the provided descriptor: " + target.getUri() );
		}

		if ( !overwriteExisting && exists( target ) ) {
			throw new IllegalArgumentException( "Unable to save file to the given descriptor: " + target.getUri() + ". File already exists." );
		}

		try {
			PutObjectRequest putObjectRequest =
					new PutObjectRequest( bucketName, buildAwsPath( target ), inputStream, new ObjectMetadata() );
			if ( target.getFolderId() != null ) {
				createFolder( target.getFolderId() );
			}
			amazonS3Client.putObject( putObjectRequest );
		}
		catch ( AmazonServiceException e ) {
			LOG.error( "Unable to save file on Amazon", e );
			throw new FileStorageException( e );
		}
		return target;
	}

	@Override
	public boolean delete( FileDescriptor descriptor ) {
		try {
			amazonS3Client.deleteObject( bucketName, buildAwsPath( descriptor ) );
		}
		catch ( AmazonServiceException e ) {
			LOG.error( "Unable to delete file on Amazon", e );
			return false;
		}
		return true;
	}

	@Override
	public OutputStream getOutputStream( FileDescriptor descriptor ) {
		return new S3OutputStream( this, descriptor );
	}

	@Override
	public InputStream getInputStream( FileDescriptor descriptor ) {
		try {
			S3Object object = amazonS3Client.getObject( bucketName, buildAwsPath( descriptor ) );
			return object.getObjectContent();
		}
		catch ( AmazonServiceException e ) {
			LOG.error( "Unable to get file {} on Amazon", descriptor, e );
			throw new FileStorageException( e );
		}
	}

	@Override
	public File getAsFile( FileDescriptor descriptor ) {
		assertValidDescriptor( descriptor );
		try {
			File localFile = fileManager.createTempFile();
			FileUtils.forceMkdir( localFile.getParentFile() );
			IOUtils.copy( getInputStream( descriptor ), new FileOutputStream( localFile ) );
			return localFile;
		}
		catch ( IOException e ) {
			throw new FileStorageException( e );
		}
		catch ( AmazonServiceException e ) {
			LOG.error( "Unable to get file {} on Amazon", descriptor, e );
			throw new FileStorageException( e );
		}
	}

	@Override
	public boolean exists( FileDescriptor descriptor ) {
		try {
			return amazonS3Client.doesObjectExist( bucketName, buildAwsPath( descriptor ) );
		}
		catch ( AmazonServiceException e ) {
			LOG.error( "Unable to check file existance {} on Amazon", descriptor, e );
			throw new FileStorageException( e );
		}
	}

	@Override
	public boolean move( FileDescriptor source, FileDescriptor target ) {
		String renamedRep = target.getRepositoryId();
		String originalRep = source.getRepositoryId();
		if ( !StringUtils.equals( originalRep, renamedRep ) ) {
			throw new IllegalArgumentException( "Repository id of the target is different from the source." );
		}
		try {
			if ( target.getFolderId() != null ) {
				createFolder( target.getFolderId() );
			}
			amazonS3Client.copyObject( bucketName, buildAwsPath( source ), bucketName, buildAwsPath( target ) );
			delete( source );
			return true;
		}
		catch ( AmazonServiceException e ) {
			LOG.error( "Unable to move {} to {} on Amazon", source, target, e );
			return false;
		}
	}

	private FileDescriptor buildNewDescriptor( String proposedName ) {
		String extension = FilenameUtils.getExtension( proposedName );
		String fileName = UUID.randomUUID() + ( !StringUtils.isBlank( extension ) ? "." + extension : "" );

		String path = pathGenerator.map( PathGenerator::generatePath ).orElse( null );

		return new FileDescriptor( repositoryId, path, fileName );
	}

	private void assertValidDescriptor( FileDescriptor descriptor ) {
		if ( !StringUtils.equals( getRepositoryId(), descriptor.getRepositoryId() ) ) {
			throw new FileStorageException( String.format(
					"Attempt to use a FileDescriptor of repository %s on repository %s", descriptor.getRepositoryId(),
					getRepositoryId() ) );
		}
	}

	private String buildAwsPath( FileDescriptor descriptor ) {
		String result;
		if ( descriptor.getFolderId() != null ) {
			result = Paths.get( descriptor.getFolderId(), descriptor.getFileId() ).toString();
		}
		else {
			result = Paths.get( descriptor.getFileId() ).toString();
		}

		return result.replace( "\\", "/" );
	}

	//found on https://stackoverflow.com/questions/11491304/amazon-web-services-aws-s3-java-create-a-sub-directory-object
	private void createFolder( String folderName ) {
		// create meta-data for your folder and set content-length to 0
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength( 0 );

		// create empty content
		InputStream emptyContent = new ByteArrayInputStream( new byte[0] );

		// create a PutObjectRequest passing the folder name suffixed by /
		PutObjectRequest putObjectRequest = new PutObjectRequest( bucketName, folderName + "/", emptyContent, metadata );

		// send request to S3 to create folder
		amazonS3Client.putObject( putObjectRequest );
	}
}
