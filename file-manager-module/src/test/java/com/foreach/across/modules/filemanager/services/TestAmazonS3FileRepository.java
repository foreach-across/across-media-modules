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
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.junit.jupiter.api.AfterAll;

import java.io.File;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestAmazonS3FileRepository extends BaseFileRepositoryTest
{
	private static final String BUCKET_NAME = "ax-filemanager-test";

	private static AmazonS3 amazonS3;

	@Override
	void createRepository() {
		if ( amazonS3 == null ) {
			amazonS3 = AmazonS3ClientBuilder.standard()
			                                .withEndpointConfiguration( new AwsClientBuilder.EndpointConfiguration( "http://localhost:4572", "us-east-1" ) )
			                                .withPathStyleAccessEnabled( true )
			                                .withCredentials( new AWSStaticCredentialsProvider( new BasicAWSCredentials( "test", "test" ) ) )
			                                .build();

			if ( !amazonS3.doesBucketExist( BUCKET_NAME ) ) {
				amazonS3.createBucket( BUCKET_NAME );
			}
		}
		FileManager fileManager = mock( FileManager.class );
		when( fileManager.createTempFile() ).thenAnswer( invoc -> new File( TEMP_DIR, UUID.randomUUID().toString() ) );

		AmazonS3FileRepository s3 = AmazonS3FileRepository.builder()
		                                                  .repositoryId( "s3-repo" )
		                                                  .amazonS3( amazonS3 )
		                                                  .bucketName( BUCKET_NAME )
		                                                  .build();
		s3.setFileManager( fileManager );

		this.fileRepository = s3;
	}

	@AfterAll
	static void tearDown() {
		amazonS3.listObjects( BUCKET_NAME ).getObjectSummaries().forEach( o -> amazonS3.deleteObject( BUCKET_NAME, o.getKey() ) );
		amazonS3.deleteBucket( BUCKET_NAME );
		amazonS3 = null;
	}
}