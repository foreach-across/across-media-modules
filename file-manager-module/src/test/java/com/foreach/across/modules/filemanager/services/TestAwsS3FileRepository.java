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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestPropertySource("${user.home}/dev-configs/across-test.properties")
public class TestAwsS3FileRepository extends BaseFileRepositoryTest
{
	public static final String AWS_REGION = "eu-west-1";
	private static String bucketName;
	private AWSCredentials credentials;

	/**
	 * Load the properties defined in {@code across-test.properties} so that they can be picked up by the {@link DefaultAWSCredentialsProviderChain}
	 * The provider chain looks for the following credentials:
	 * - aws.accessKeyId
	 * - aws.secretKey
	 * - aws.sessionToken (optional)
	 */
	@BeforeClass
	public static void setUp() throws IOException {
		System.getProperties().load( new FileReader( new File( System.getProperty( "user.home" ) + "/dev-configs/across-test.properties" ) ) );
	}

	/**
	 * To create a {@code AwsS3FileRepository}, get credentials using {@code DefaultAWSCrednetialsProviderChain} as described
	 * on http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html
	 */
	public void createRepository() {
		bucketName = "ax-filemanager-test";
		FileManager fileManager = mock( FileManager.class );
		when( fileManager.createTempFile() ).thenAnswer( invoc -> new File( TEMP_DIR, UUID.randomUUID().toString() ) );
		credentials = new DefaultAWSCredentialsProviderChain().getCredentials();
		if ( StringUtils.isAnyBlank( credentials.getAWSAccessKeyId(), credentials.getAWSSecretKey() ) ) {
			throw new RuntimeException(
					"For running this test, you need to provide access keys for Amazon, please see http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html for more information on how to setup AWS credentials" );
		}
		fileRepository = new AwsS3FileRepository( bucketName,
		                                          credentials.getAWSAccessKeyId(),
		                                          credentials.getAWSSecretKey(),
		                                          fileManager,
		                                          AWS_REGION,
		                                          DateFormatPathGenerator.YEAR_MONTH_DAY );
	}

	@AfterClass
	public static void tearDown() throws Exception {
		//delete all entries from the bucket
		AmazonS3 s3 = AmazonS3ClientBuilder.standard()
		                                   .withRegion( AWS_REGION )
		                                   .withCredentials( new DefaultAWSCredentialsProviderChain() )
		                                   .build();
		ObjectListing object_listing = s3.listObjects( bucketName );
		while ( true ) {
			for ( Iterator<?> iterator =
			      object_listing.getObjectSummaries().iterator();
			      iterator.hasNext(); ) {
				S3ObjectSummary summary = (S3ObjectSummary) iterator.next();
				s3.deleteObject( bucketName, summary.getKey() );
			}

			// more object_listing to retrieve?
			if ( object_listing.isTruncated() ) {
				object_listing = s3.listNextBatchOfObjects( object_listing );
			}
			else {
				break;
			}
		}
		;

		VersionListing version_listing = s3.listVersions(
				new ListVersionsRequest().withBucketName( bucketName ) );
		while ( true ) {
			for ( Iterator<?> iterator =
			      version_listing.getVersionSummaries().iterator();
			      iterator.hasNext(); ) {
				S3VersionSummary vs = (S3VersionSummary) iterator.next();
				s3.deleteVersion(

						bucketName, vs.getKey(), vs.getVersionId() );
			}

			if ( version_listing.isTruncated() ) {
				version_listing = s3.listNextBatchOfVersions(
						version_listing );
			}
			else {
				break;
			}
		}

	}
}