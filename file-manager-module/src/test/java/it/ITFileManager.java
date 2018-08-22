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

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.across.modules.filemanager.services.FileRepository;
import com.foreach.across.modules.filemanager.services.FileRepositoryRegistry;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ITFileManager extends AbstractFileManagerModuleIT
{
	private static final Resource RES_TEXTFILE = new ClassPathResource( "textfile.txt" );

	private static final String TEMP_DIR = System.getProperty( "java.io.tmpdir" );
	private static final String ROOT_DIR = Paths.get( TEMP_DIR, UUID.randomUUID().toString() ).toString();

	@Autowired
	private FileManager fileManager;

	@Autowired
	private FileRepositoryRegistry fileRepositoryRegistry;

	@Test
	public void moveFile() throws IOException {
		FileDescriptor file = fileManager.save( RES_TEXTFILE.getInputStream() );
		FileDescriptor firstMoved = new FileDescriptor( file.getRepositoryId(), file.getFolderId(),
		                                                "renamed-" + file.getFileId() );
		fileManager.move( file, firstMoved );
		fileManager.exists( firstMoved );
		FileRepository defaultRep = fileManager.getRepository( file.getRepositoryId() );
		assertTrue( defaultRep.getAsFile( firstMoved ).exists() );
		assertFalse( defaultRep.getAsFile( file ).exists() );

		FileRepository moveIt = fileRepositoryRegistry.getRepository( "move-it" );
		FileDescriptor secondMoved = new FileDescriptor( moveIt.getRepositoryId(), file.getFolderId(),
		                                                 firstMoved.getFileId() );
		fileManager.move( firstMoved, secondMoved );
		fileManager.exists( secondMoved );
		assertTrue( moveIt.getAsFile( secondMoved ).exists() );
		assertFalse( defaultRep.getAsFile( firstMoved ).exists() );
		assertFalse( defaultRep.getAsFile( file ).exists() );
	}
}
