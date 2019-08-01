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

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * Old implementation which creates an Amazon S3 client itself.
 * Kept for backwards compatibility but deprecated in favour or {@link AmazonS3FileRepository},
 * which requires the {@code AmazonS3} client to be passed in.
 *
 * @author Sander Van Loock
 * @since 1.2.0
 * @deprecated since 1.4.0 - will be removed in 2.0.0, use {@link AmazonS3FileRepository} instead
 */
@Deprecated
@SuppressWarnings("all")
public class AwsS3FileRepository extends AmazonS3FileRepository
{
	public static final String DEFAULT_REGION = "eu-central-1";

	@Deprecated
	public AwsS3FileRepository( String bucketName, String accessKey, String accessSecret, FileManager fileManager ) {
		this( bucketName, accessKey, accessSecret, fileManager, DEFAULT_REGION, null );
	}

	@Deprecated
	public AwsS3FileRepository( String bucketName, String accessKey, String accessSecret, FileManager fileManager, String region ) {
		this( bucketName, accessKey, accessSecret, fileManager, region, null );
	}

	@Deprecated
	public AwsS3FileRepository( String bucketName,
	                            String accessKey,
	                            String accessSecret,
	                            FileManager fileManager,
	                            String region,
	                            PathGenerator pathGenerator ) {
		this( bucketName, bucketName, accessKey, accessSecret, fileManager, region, pathGenerator );
	}

	@Deprecated
	public AwsS3FileRepository( String repositoryId,
	                            String bucketName,
	                            String accessKey,
	                            String accessSecret,
	                            FileManager fileManager,
	                            String region,
	                            PathGenerator pathGenerator ) {
		this(
				repositoryId,
				bucketName,
				AmazonS3ClientBuilder.standard()
				                     .withRegion( StringUtils.isNoneBlank( region ) ? region : DEFAULT_REGION )
				                     .withCredentials( new AWSStaticCredentialsProvider( new BasicAWSCredentials( accessKey, accessSecret ) ) )
				                     .build(),
				Optional.ofNullable( pathGenerator ),
				fileManager
		);
	}

	@Deprecated
	public AwsS3FileRepository( String repositoryId,
	                            String bucketName,
	                            AmazonS3 amazonS3Client,
	                            Optional<PathGenerator> pathGenerator,
	                            FileManager fileManager ) {
		super( repositoryId, amazonS3Client, bucketName, pathGenerator != null ? pathGenerator.orElse( null ) : null, null );
		setFileManager( fileManager );
	}
}
