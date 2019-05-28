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

package com.foreach.across.modules.filemanager.business;

import lombok.SneakyThrows;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;

import static com.foreach.across.modules.filemanager.business.FileDescriptor.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestFileDescriptor
{
	@Test
	public void generateUri() {
		assertEquals( "default:test-file.txt", uri( "default", "test-file.txt" ) );
		assertEquals( "amazon-s3:2014/15/06:123-456-798", uri( "amazon-s3", "2014/15/06", "123-456-798" ) );
	}

	@Test
	public void parseUri() {
		FileDescriptor descriptor = parse( "default:test-file.txt" );

		assertEquals( "default", descriptor.getRepositoryId() );
		assertNull( descriptor.getFolderId() );
		assertEquals( "test-file.txt", descriptor.getFileId() );

		descriptor = parse( "amazon-s3:2014/15/06:123-456-798" );

		assertEquals( "amazon-s3", descriptor.getRepositoryId() );
		assertEquals( "2014/15/06", descriptor.getFolderId() );
		assertEquals( "123-456-798", descriptor.getFileId() );

		assertEquals( of( "my-repository", null, "myfile" ), of( "my-repository:myfile" ) );
		assertEquals( of( "my-repository", null, "myfile" ), of( "my-repository::myfile" ) );
		assertEquals( of( "my-repository", "my/folder", "myfile" ), of( "my-repository:my/folder:myfile" ) );
		assertEquals( of( "my-repository", "my/folder", "myfile" ), of( "my-repository:my/folder/myfile" ) );
		assertEquals( of( "my-repository", "my/folder", "myfile" ), of( "my-repository:my\\folder\\myfile" ) );
		assertEquals( of( "my-repository", "my/folder", "/folder/myfile" ), of( "my-repository:my/folder:/folder/myfile" ) );
	}

	@Test
	@SneakyThrows
	public void toResourceUri() {
		FileDescriptor descriptor = of( "my-repo", "my/folder", "test-file.txt" );
		assertThat( descriptor.toResourceURI().toString() ).isEqualTo( "axfs://my-repo:my/folder:test-file.txt" );
	}

	@Test
	public void uriMayNotBeNull() {
		String emptyUri = "uri may not be null or empty";
		assertIllegalArgumentException( () -> of( null ), emptyUri );
		assertIllegalArgumentException( () -> of( "" ), emptyUri );
		assertIllegalArgumentException( () -> of( null ), emptyUri );
		String buildUriMessage = "both a repositoryId and a fileId are required to build a valid uri";
		assertIllegalArgumentException( () -> FileDescriptor.buildUri( null, null, null ), buildUriMessage );
		assertIllegalArgumentException( () -> FileDescriptor.buildUri( "", null, null ), buildUriMessage );
		assertIllegalArgumentException( () -> FileDescriptor.buildUri( null, null, "" ), buildUriMessage );
		assertIllegalArgumentException( () -> FileDescriptor.buildUri( null, null, "my-file.txt" ), buildUriMessage );
		assertIllegalArgumentException( () -> FileDescriptor.buildUri( "", null, "" ), buildUriMessage );
	}

	private void assertIllegalArgumentException( ThrowableAssert.ThrowingCallable callable, String msg ) {
		assertThatThrownBy( callable ).isInstanceOf( IllegalArgumentException.class )
		                              .hasMessage( msg );
	}

	private FileDescriptor parse( String uri ) {
		return new FileDescriptor( uri );
	}

	private String uri( String repository, String fileId ) {
		return new FileDescriptor( repository, fileId ).getUri();
	}

	private String uri( String repository, String folderId, String fileId ) {
		return new FileDescriptor( repository, folderId, fileId ).getUri();
	}
}
