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

package com.foreach.across.modules.filemanager.business.reference;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestFileReference
{
	@Test
	void builder() {
		FileReference fileReference = FileReference.builder().build();
		assertThat( fileReference.getUuid() ).isNotBlank();

		FileDescriptor fileDescriptor = FileDescriptor.of( "test:some-file.txt" );
		fileReference = FileReference.builder()
		                             .fileSize( 4324L )
		                             .mimeType( "text" )
		                             .fileDescriptor( fileDescriptor )
		                             .build();

		assertThat( fileReference ).hasFieldOrPropertyWithValue( "fileSize", 4324L )
		                           .hasFieldOrPropertyWithValue( "mimeType", "text" )
		                           .hasFieldOrPropertyWithValue( "fileDescriptor", fileDescriptor );
		assertThat( fileReference.getUuid() ).isNotBlank();

		fileReference = FileReference.builder()
		                             .fileDescriptor( fileDescriptor )
		                             .name( "some-file.txt" )
		                             .uuid( "my:personal-uuid" )
		                             .build();
		assertThat( fileReference ).hasFieldOrPropertyWithValue( "fileDescriptor", fileDescriptor )
		                           .hasFieldOrPropertyWithValue( "name", "some-file.txt" )
		                           .hasFieldOrPropertyWithValue( "uuid", "my:personal-uuid" );
	}

	@Test
	void uuidIsSetIfNotPresent() {
		FileReference fileReference = new FileReference();
		assertThat( fileReference.getUuid() ).isNotBlank();
	}

	@Test
	void fileDescriptor() {
		String uri = "default:test-file.txt";
		FileDescriptor fileDescriptor = new FileDescriptor( uri );

		FileReference fileReference = new FileReference();
		fileReference.setFileDescriptor( fileDescriptor );
		assertThat( fileReference ).hasFieldOrPropertyWithValue( "fileDescriptor", fileDescriptor );
		assertThat( fileReference.getFileDescriptor() ).isEqualTo( fileDescriptor );

		fileReference = new FileReference( 0L, "my-original-id", "Some name", fileDescriptor, null, null, null );
		assertThat( fileReference )
				.hasFieldOrPropertyWithValue( "id", 0L )
				.hasFieldOrPropertyWithValue( "uuid", "my-original-id" )
				.hasFieldOrPropertyWithValue( "name", "Some name" )
				.hasFieldOrPropertyWithValue( "fileDescriptor", fileDescriptor )
				.hasFieldOrPropertyWithValue( "fileSize", null )
				.hasFieldOrPropertyWithValue( "mimeType", null )
				.hasFieldOrPropertyWithValue( "hash", null );
	}
}
