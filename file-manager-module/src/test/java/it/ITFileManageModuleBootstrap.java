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

import com.foreach.across.core.AcrossConfigurationException;
import com.foreach.across.modules.bootstrapui.BootstrapUiModule;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.filemanager.FileManagerModule;
import com.foreach.across.modules.filemanager.FileManagerModuleIcons;
import com.foreach.across.modules.filemanager.business.reference.FileReferenceService;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.properties.PropertiesModule;
import com.foreach.across.test.AcrossTestContext;
import org.junit.jupiter.api.Test;

import static com.foreach.across.test.support.AcrossTestBuilders.standard;
import static com.foreach.across.test.support.AcrossTestBuilders.web;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
class ITFileManageModuleBootstrap
{
	@Test
	void fileManagerModule() {
		try (AcrossTestContext ctx = standard().modules( FileManagerModule.NAME ).build()) {
			assertThat( ctx.contextInfo().isBootstrapped() ).isTrue();
			assertThat( ctx.contextInfo().getModuleInfo( FileManagerModule.NAME ).getApplicationContext().getBeansOfType( FileReferenceService.class ) )
					.isEmpty();

			assertThatExceptionOfType( IllegalArgumentException.class )
					.isThrownBy( FileManagerModuleIcons.fileManagerIcons::removeFile );
		}
	}

	@Test
	void fileManagerModuleAndPropertiesModule() {
		try (AcrossTestContext ctx = web().modules( FileManagerModule.NAME, PropertiesModule.NAME )
		                                  .build()) {
			assertThat( ctx.contextInfo().isBootstrapped() ).isTrue();
			assertThat( ctx.contextInfo().getModuleInfo( FileManagerModule.NAME ).getApplicationContext().getBeansOfType( FileReferenceService.class ) )
					.isEmpty();

			assertThatExceptionOfType( IllegalArgumentException.class )
					.isThrownBy( FileManagerModuleIcons.fileManagerIcons::removeFile );
		}
	}

	@Test
	void fileManagerModuleAndHibernateModule() {
		try (AcrossTestContext ctx = web().modules( FileManagerModule.NAME, AcrossHibernateJpaModule.NAME )
		                                  .build()) {
			assertThat( ctx.contextInfo().isBootstrapped() ).isFalse();
		}
		catch ( AcrossConfigurationException e ) {
			assertThat( e.getMessage() ).isEqualTo( FileManagerModule.NAME + " requires " + PropertiesModule.NAME
					                                        + " to be present when " + AcrossHibernateJpaModule.NAME + " is configured." );

		}
	}

	@Test
	void fileManagerModuleAndHibernateModuleAndPropertiesModule() {
		try (AcrossTestContext ctx = web().modules( FileManagerModule.NAME, AcrossHibernateJpaModule.NAME, PropertiesModule.NAME )
		                                  .build()) {
			assertThat( ctx.contextInfo().isBootstrapped() ).isTrue();
			assertThat( ctx.contextInfo().getModuleInfo( FileManagerModule.NAME ).getApplicationContext().getBeansOfType( FileReferenceService.class ) )
					.hasSize( 1 );

			assertThatExceptionOfType( IllegalArgumentException.class )
					.isThrownBy( FileManagerModuleIcons.fileManagerIcons::removeFile );
		}
	}

	@Test
	void entityModule() {
		try (AcrossTestContext ctx = web().modules( FileManagerModule.NAME, BootstrapUiModule.NAME, EntityModule.NAME ).build()) {
			assertThat( ctx.contextInfo().isBootstrapped() ).isTrue();
			assertThat( ctx.contextInfo().getModuleInfo( FileManagerModule.NAME ).getApplicationContext().getBeansOfType( FileReferenceService.class ) )
					.isEmpty();

			assertThat( FileManagerModuleIcons.fileManagerIcons.removeFile() ).isNotNull();
		}
	}
}
