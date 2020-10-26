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

package it;

import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FileStorageException;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.across.modules.filemanager.services.FileRepository;
import com.foreach.across.modules.filemanager.services.FileRepositoryRegistry;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ITFileManager extends AbstractFileManagerModuleIT
{
	private static final Resource RES_TEXTFILE = new ClassPathResource( "textfile.txt" );

	@Autowired
	private FileManager fileManager;

	@Autowired
	private FileRepositoryRegistry fileRepositoryRegistry;

	@Test
	void bothTestAndDefaultRepositoryShouldBeAvailable() {
		assertNotNull( fileManager );
		assertNotNull( fileManager.getRepository( FileManager.TEMP_REPOSITORY ) );
		assertNotNull( fileManager.getRepository( FileManager.DEFAULT_REPOSITORY ) );
	}

	@Test
	void fileCanBeStoredInDefaultRepository() throws IOException {
		FileDescriptor file = fileManager.save( RES_TEXTFILE.getInputStream() );

		assertNotNull( file );
		assertTrue( fileManager.exists( file ) );
	}

	@Test
	void moveFile() throws IOException {
		FileDescriptor file = fileManager.save( RES_TEXTFILE.getInputStream() );
		FileDescriptor firstMoved = FileDescriptor.of( file.getRepositoryId(), file.getFolderId(), "renamed-" + file.getFileId() );
		fileManager.move( file, firstMoved );
		fileManager.exists( firstMoved );
		FileRepository defaultRep = fileManager.getRepository( file.getRepositoryId() );
		assertTrue( defaultRep.getAsFile( firstMoved ).exists() );

		assertThatExceptionOfType( FileStorageException.class )
				.isThrownBy( () -> defaultRep.getAsFile( file ) )
				.withCauseInstanceOf( FileNotFoundException.class );

		FileRepository moveIt = fileRepositoryRegistry.getRepository( "move-it" );
		FileDescriptor secondMoved = FileDescriptor.of( moveIt.getRepositoryId(), file.getFolderId(), firstMoved.getFileId() );
		fileManager.move( firstMoved, secondMoved );
		fileManager.exists( secondMoved );
		assertTrue( moveIt.getAsFile( secondMoved ).exists() );

		assertThatExceptionOfType( FileStorageException.class )
				.isThrownBy( () -> defaultRep.getAsFile( firstMoved ) )
				.withCauseInstanceOf( FileNotFoundException.class );

		assertThatExceptionOfType( FileStorageException.class )
				.isThrownBy( () -> defaultRep.getAsFile( file ) )
				.withCauseInstanceOf( FileNotFoundException.class );
	}

	@Test
	@SneakyThrows
	void resourceResolving( @Autowired AcrossContextInfo contextInfo, @Autowired ApplicationContext parentContext ) {
		ApplicationContext applicationContext = contextInfo.getModuleInfo( "testModule" ).getApplicationContext();

		FileResource fileResource = fileManager.getFileResource( FileDescriptor.of( "default:my-resource-file.txt" ) );
		fileResource.copyFrom( RES_TEXTFILE );

		Resource fromModuleContext = applicationContext.getResource( "axfs://default:my-resource-file.txt" );
		assertThat( fromModuleContext.exists() ).isTrue();
		assertThat( fromModuleContext ).isEqualTo( fileResource );

		Resource fromParentContext = parentContext.getResource( "axfs://default:my-resource-file.txt" );
		assertThat( fromParentContext.exists() ).isTrue();
		assertThat( fromParentContext ).isEqualTo( fileResource );

		assertThat( applicationContext.getResources( "axfs://default:my-resource-*.txt" ) ).contains( fileResource );
		assertThat( parentContext.getResources( "axfs://default:my-resource-*.txt" ) ).contains( fileResource );
	}
}
