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
import com.foreach.across.modules.filemanager.business.FileResource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.util.StreamUtils;

import java.io.OutputStream;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;

class TestCachingFileRepositoryLocalAndS3 extends BaseFileRepositoryTest
{
	private static final String BUCKET_NAME = "ax-filemanager-test";

	private static AmazonS3 amazonS3;

	private AmazonS3FileRepository remoteRepository;
	private LocalFileRepository cacheRepository;

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
		FileManagerImpl fileManager = new FileManagerImpl();

		remoteRepository = AmazonS3FileRepository.builder()
		                                         .repositoryId( "s3-repo" )
		                                         .amazonS3( amazonS3 )
		                                         .bucketName( BUCKET_NAME )
		                                         .build();
		remoteRepository.setFileManager( fileManager );

		CachingFileRepository repositoryToTest = CachingFileRepository.withGeneratedFileDescriptor()
		                                                              .targetFileRepository( remoteRepository )
		                                                              .cacheRepositoryId( "cache" )
		                                                              .build();
		repositoryToTest.setFileManager( fileManager );

		cacheRepository = new LocalFileRepository( "cache", ROOT_DIR );
		fileManager.registerRepository( cacheRepository );

		this.fileRepository = repositoryToTest;

	}

	@Test
	@SneakyThrows
	void cacheResourceMayBeRemoved() {
		CachedFileResource resource = (CachedFileResource) fileRepository.createFileResource();
		resource.copyFrom( RES_TEXTFILE );

		LocalFileResource cache = (LocalFileResource) resource.getCache();
		AmazonS3FileResource target = (AmazonS3FileResource) resource.getTarget();

		assertThat( readResource( resource ) ).isEqualTo( "some dummy text" );
		assertThat( readResource( target ) ).isEqualTo( "some dummy text" );
		assertThat( readResource( cache ) ).isEqualTo( "some dummy text" );
		assertThat( resource.contentLength() )
				.isEqualTo( target.contentLength() )
				.isEqualTo( resource.getCache().contentLength() );

		assertThat( cache.getTargetFile().delete() ).isTrue();

		assertThat( cache.exists() ).isFalse();
		assertThat( readResource( target ) ).isEqualTo( "some dummy text" );
		assertThat( cache.exists() ).isFalse();
		assertThat( readResource( resource ) ).isEqualTo( "some dummy text" );
		assertThat( cache.exists() ).isTrue();
		assertThat( readResource( cache ) ).isEqualTo( "some dummy text" );

		resource.flushCache();
		assertThat( cache.exists() ).isFalse();
		assertThat( readResource( resource ) ).isEqualTo( "some dummy text" );
		assertThat( readResource( cache ) ).isEqualTo( "some dummy text" );

		target.delete();
		assertThat( readResource( cache ) ).isEqualTo( "some dummy text" );
		assertThat( readResource( resource ) ).isEqualTo( "some dummy text" );
		assertThat( resource.exists() ).isTrue();
		assertThat( cache.exists() ).isTrue();
		assertThat( target.exists() ).isFalse();

		resource.flushCache();
		assertThat( resource.exists() ).isFalse();
		assertThat( cache.exists() ).isFalse();
		assertThat( target.exists() ).isFalse();
	}

	@Test
	@SneakyThrows
	void updatesGoToCacheAndTargetImmediately() {
		CachedFileResource resource = (CachedFileResource) fileRepository.createFileResource();
		resource.copyFrom( RES_TEXTFILE );

		LocalFileResource cache = (LocalFileResource) resource.getCache();
		AmazonS3FileResource target = (AmazonS3FileResource) resource.getTarget();

		try (OutputStream os = resource.getOutputStream()) {
			StreamUtils.copy( "updated data", Charset.defaultCharset(), os );
		}

		assertThat( readResource( resource ) ).isEqualTo( "updated data" );
		assertThat( readResource( cache ) ).isEqualTo( "updated data" );
		assertThat( readResource( target ) ).isEqualTo( "updated data" );
	}

	@Test
	@SneakyThrows
	void deleteRemovesBothOriginalAndTarget() {
		CachedFileResource resource = (CachedFileResource) fileRepository.createFileResource();
		resource.copyFrom( RES_TEXTFILE );

		LocalFileResource cache = (LocalFileResource) resource.getCache();
		AmazonS3FileResource target = (AmazonS3FileResource) resource.getTarget();

		resource.delete();
		assertThat( resource.exists() ).isFalse();
		assertThat( cache.exists() ).isFalse();
		assertThat( target.exists() ).isFalse();
	}

	@Test
	@SneakyThrows
	void individualFileResources() {
		CachedFileResource resource = (CachedFileResource) fileRepository.createFileResource();
		resource.copyFrom( RES_TEXTFILE );

		LocalFileResource cache = (LocalFileResource) resource.getCache();
		AmazonS3FileResource target = (AmazonS3FileResource) resource.getTarget();

		FileResource remote = remoteRepository.getFileResource( target.getFileDescriptor() );
		assertThat( remote.exists() ).isTrue();
		assertThat( readResource( remote ) ).isEqualTo( readResource( target ) ).isEqualTo( "some dummy text" );

		FileResource local = cacheRepository.getFileResource( cache.getFileDescriptor() );
		assertThat( local.exists() ).isTrue();
		assertThat( readResource( local ) ).isEqualTo( readResource( cache ) ).isEqualTo( "some dummy text" );
	}

	@AfterAll
	static void tearDown() {
		amazonS3 = null;
	}
}