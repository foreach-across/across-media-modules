package com.foreach.across.modules.filemanager.it;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.filemanager.FileManagerModule;
import com.foreach.across.modules.filemanager.FileManagerModuleSettings;
import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.across.test.AcrossTestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = ITFileManagerModule.Config.class)
public class ITFileManagerModule
{
	private static final Resource RES_TEXTFILE = new ClassPathResource( "textfile.txt" );

	@Autowired(required = false)
	private FileManager fileManager;

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

	@Configuration
	@AcrossTestConfiguration
	protected static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			FileManagerModule module = new FileManagerModule();
			module.setProperty( FileManagerModuleSettings.LOCAL_REPOSITORIES_ROOT, System.getProperty(
					"java.io.tmpdir" ) );

			context.addModule( module );
		}
	}
}
