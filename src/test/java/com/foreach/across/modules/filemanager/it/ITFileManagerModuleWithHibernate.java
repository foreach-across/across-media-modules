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

import com.foreach.across.modules.filemanager.FileManagerModule;
import com.foreach.across.modules.filemanager.FileManagerModuleSettings;
import com.foreach.across.modules.filemanager.business.FileReferenceRepository;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.across.test.AcrossWebAppConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@AcrossWebAppConfiguration
public class ITFileManagerModuleWithHibernate
{
	@Autowired
	private FileReferenceRepository fileReferenceRepository;

	@Test
	public void repositoryIsCreated() {
		assertNotNull( fileReferenceRepository );
	}

	@AcrossTestConfiguration(modules = AcrossHibernateJpaModule.NAME)
	protected static class Config
	{
		@Bean
		public FileManagerModule fileManagerModule() {
			FileManagerModule module = new FileManagerModule();
			module.setProperty(
					FileManagerModuleSettings.LOCAL_REPOSITORIES_ROOT,
					System.getProperty( "java.io.tmpdir" )
			);
			return module;
		}
	}
}
