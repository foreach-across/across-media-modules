package it;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.reference.FileReferenceRepository;
import com.foreach.across.modules.filemanager.services.FileManager;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
public class ITFileManagerModule extends AbstractFileManagerModuleIT
{
	private static final Resource RES_TEXTFILE = new ClassPathResource( "textfile.txt" );

	@Autowired(required = false)
	private FileManager fileManager;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void repositoryDoesNotExist() {
		assertThat( applicationContext.getParent().getBeansOfType( FileReferenceRepository.class ) ).isEmpty();
	}

	@Test
	public void bothTestAndDefaultRepositoryShouldBeAvailable() {
		assertNotNull( fileManager );
		assertNotNull( fileManager.getRepository( FileManager.TEMP_REPOSITORY ) );
		assertNotNull( fileManager.getRepository( FileManager.DEFAULT_REPOSITORY ) );
	}

	@Test
	public void fileCanBeStoredInDefaultRepository() throws IOException {
		FileDescriptor file = fileManager.save( RES_TEXTFILE.getInputStream() );

		assertNotNull( file );
		assertTrue( fileManager.exists( file ) );
	}
}
