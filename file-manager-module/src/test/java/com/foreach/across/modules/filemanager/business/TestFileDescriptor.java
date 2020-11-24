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
import org.junit.jupiter.api.Test;

import static com.foreach.across.modules.filemanager.business.FileDescriptor.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestFileDescriptor
{
	@Test
	void generateUri() {
		assertEquals( "default:test-file.txt", FileDescriptor.of( "default", "test-file.txt" ).getUri() );
		assertEquals( "amazon-s3:2014/15/06:123-456-798", FileDescriptor.of( "amazon-s3", "2014/15/06", "123-456-798" ).getUri() );
	}

	@Test
	void extensionAndSuffix() {
		assertThat( FileDescriptor.of( "default:myfile" ).getExtension() ).isEmpty();
		assertThat( FileDescriptor.of( "default:myfile.list.jpeg" ).getExtension() ).isEqualTo( "jpeg" );

		assertThat( FileDescriptor.of( "default:myfile" ).withSuffix( "" ) ).isEqualTo( FileDescriptor.of( "default:myfile" ) );
		assertThat( FileDescriptor.of( "default:myfile" ).withSuffix( null ) ).isEqualTo( FileDescriptor.of( "default:myfile" ) );
		assertThat( FileDescriptor.of( "default:myfile" ).withSuffix( "_some.txt" ) ).isEqualTo( FileDescriptor.of( "default:myfile_some.txt" ) );
		assertThat( FileDescriptor.of( "default:myfile" ).withSuffix( "txt" ) ).isEqualTo( FileDescriptor.of( "default:myfiletxt" ) );

		assertThat( FileDescriptor.of( "default:myfile" ).withExtension( "txt" ) ).isEqualTo( FileDescriptor.of( "default:myfile.txt" ) );
		assertThat( FileDescriptor.of( "default:myfile" ).withExtension( ".txt" ) ).isEqualTo( FileDescriptor.of( "default:myfile.txt" ) );
		assertThat( FileDescriptor.of( "default:myfile.doc" ).withExtension( ".txt" ) ).isEqualTo( FileDescriptor.of( "default:myfile.txt" ) );
		assertThat( FileDescriptor.of( "default:myfile.doc.html" ).withExtension( "txt" ) ).isEqualTo( FileDescriptor.of( "default:myfile.doc.txt" ) );
		assertThat( FileDescriptor.of( "default:myfile.doc" ).withExtension( "" ) ).isEqualTo( FileDescriptor.of( "default:myfile" ) );
		assertThat( FileDescriptor.of( "default:myfile.doc" ).withExtension( null ) ).isEqualTo( FileDescriptor.of( "default:myfile" ) );

		assertThat( FileDescriptor.of( "default:myfile.doc" ).withExtensionFrom( null ) ).isEqualTo( FileDescriptor.of( "default:myfile" ) );
		assertThat( FileDescriptor.of( "default:myfile.doc" ).withExtensionFrom( "" ) ).isEqualTo( FileDescriptor.of( "default:myfile" ) );
		assertThat( FileDescriptor.of( "default:myfile.doc" ).withExtensionFrom( "/my.dir/my" ) ).isEqualTo( FileDescriptor.of( "default:myfile" ) );
		assertThat( FileDescriptor.of( "default:myfile.doc" ).withExtensionFrom( "c:/my.dir/my.file.xls" ) ).isEqualTo(
				FileDescriptor.of( "default:myfile.xls" ) );
	}

	@Test
	void parseUri() {
		FileDescriptor descriptor = FileDescriptor.of( "default:test-file.txt" );

		assertEquals( "default", descriptor.getRepositoryId() );
		assertNull( descriptor.getFolderId() );
		assertEquals( "test-file.txt", descriptor.getFileId() );

		descriptor = FileDescriptor.of( "amazon-s3:2014/15/06:123-456-798" );

		assertEquals( "amazon-s3", descriptor.getRepositoryId() );
		assertEquals( "2014/15/06", descriptor.getFolderId() );
		assertEquals( "123-456-798", descriptor.getFileId() );
		assertEquals( FolderDescriptor.of( "amazon-s3", "2014/15/06" ), descriptor.getFolderDescriptor() );

		assertEquals( of( "my-repository", null, "myfile" ), of( "my-repository:myfile" ) );
		assertEquals( of( "my-repository", null, "myfile" ), of( "axfs://my-repository::myfile" ) );
		assertEquals( of( "my-repository", "my/folder", "myfile" ), of( "my-repository:my/folder:myfile" ) );
		assertEquals( of( "my-repository", "my/folder", "myfile" ), of( "my-repository:my/folder/myfile" ) );
		assertEquals( of( "my-repository", "my/folder", "myfile" ), of( "axfs://my-repository:my/folder/myfile" ) );
		assertEquals( of( "my-repository", "my/folder", "myfile" ), of( "my-repository:my\\folder\\myfile" ) );
		assertEquals( of( "my-repository", "my/folder", "/folder/myfile" ), of( "my-repository:my/folder:/folder/myfile" ) );
		assertEquals( of( "my-repository", "my/folder", "/folder/myfile" ), of( "axfs://my-repository:my/folder:/folder/myfile" ) );
	}

	@Test
	@SneakyThrows
	void toResourceUri() {
		FileDescriptor descriptor = of( "my-repo", "my/folder", "test-file.txt" );
		assertThat( descriptor.toResourceURI().toString() ).isEqualTo( "axfs://my-repo:my/folder:test-file.txt" );
	}

	@Test
	void uriMayNotBeNull() {
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> of( null ) );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> of( "" ) );
	}

	@Test
	void fileIdMustAlwaysBePresent() {
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> of( "repo", null, "" ) );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> of( "repo", null, null ) );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> of( "repo:" ) );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> of( "repo:123/456:" ) );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> of( "repo:/123/456/" ) );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> of( "repo:/" ) );
	}
}
