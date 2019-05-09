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

import cloud.localstack.DockerTestUtils;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(randomizePorts = true, services = { "s3" })
class TestAmazonS3FileRepository extends BaseFileRepositoryTest
{
	private static final String BUCKET_NAME = "ax-filemanager-test";

	private static AmazonS3 amazonS3;

	@Override
	void createRepository() {
		if ( amazonS3 == null ) {
			amazonS3 = DockerTestUtils.getClientS3();

			if ( !amazonS3.doesBucketExist( BUCKET_NAME ) ) {
				amazonS3.createBucket( BUCKET_NAME );
			}
		}
		FileManager fileManager = mock( FileManager.class );
		when( fileManager.createTempFile() ).thenAnswer( invoc -> new File( TEMP_DIR, UUID.randomUUID().toString() ) );

		AmazonS3FileRepository s3 = AmazonS3FileRepository.builder()
		                                                  .repositoryId( "s3-repo" )
		                                                  .amazonS3( DockerTestUtils.getClientS3() )
		                                                  .bucketName( BUCKET_NAME )
		                                                  .build();
		s3.setFileManager( fileManager );

		this.fileRepository = s3;
	}

	@AfterAll
	static void tearDown() {
		amazonS3 = null;
	}
}