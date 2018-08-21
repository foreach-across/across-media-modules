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

import com.foreach.across.modules.filemanager.business.file.reference.FileReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestFileReference
{
	@Test
	public void idIsSetIfNotPresent() {
		FileReference fileReference = new FileReference();
		assertThat( fileReference.getId() ).isNotBlank();
	}

	@Test
	public void fileDescriptor() {
		String uri = "default:test-file.txt";
		FileDescriptor fileDescriptor = new FileDescriptor( uri );

		FileReference fileReference = new FileReference();
		fileReference.setFileDescriptor( fileDescriptor );
		assertThat( fileReference ).hasFieldOrPropertyWithValue( "fileDescriptor", fileDescriptor );
		assertThat( fileReference.getFileDescriptor() ).isEqualTo( fileDescriptor );

		fileReference = new FileReference( "my-original-id", "Some name", fileDescriptor, null, null, null );
		assertThat( fileReference )
				.hasFieldOrPropertyWithValue( "id", "my-original-id" )
				.hasFieldOrPropertyWithValue( "name", "Some name" )
				.hasFieldOrPropertyWithValue( "fileDescriptor", fileDescriptor )
				.hasFieldOrPropertyWithValue( "fileSize", null )
				.hasFieldOrPropertyWithValue( "mimeType", null )
				.hasFieldOrPropertyWithValue( "hash", null );
	}
}
