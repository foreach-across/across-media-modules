package com.foreach.across.modules.filemanager.it;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.across.modules.filemanager.services.FileRepository;
import com.foreach.across.modules.filemanager.services.FileRepositoryRegistry;
import com.foreach.across.modules.filemanager.services.LocalFileRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = ITFileManagerModule.Config.class)
public class ITFileManager
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
		FileDescriptor secondMoved = new FileDescriptor( moveIt.getRepositoryId(), file.getFolderId(), firstMoved.getFileId() );
		fileManager.move( firstMoved, secondMoved );
		fileManager.exists( secondMoved );
		assertTrue( moveIt.getAsFile( secondMoved ).exists() );
		assertFalse( defaultRep.getAsFile( firstMoved ).exists() );
		assertFalse( defaultRep.getAsFile( file ).exists() );
	}
}
