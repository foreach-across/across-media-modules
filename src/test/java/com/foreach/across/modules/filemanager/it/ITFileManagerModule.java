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
			module.setProperty(
					FileManagerModuleSettings.LOCAL_REPOSITORIES_ROOT,
					System.getProperty( "java.io.tmpdir" )
			);

			context.addModule( module );
		}
	}
}
