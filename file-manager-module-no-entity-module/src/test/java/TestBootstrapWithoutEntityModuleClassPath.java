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

import com.foreach.across.modules.filemanager.FileManagerModule;
import com.foreach.across.modules.filemanager.business.reference.FileReferenceInterceptor;
import com.foreach.across.modules.filemanager.business.reference.MultipartFileToFileReferenceConverter;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.properties.PropertiesModule;
import com.foreach.across.test.AcrossTestContext;
import org.junit.jupiter.api.Test;

import static com.foreach.across.test.support.AcrossTestBuilders.web;
import static java.lang.Thread.currentThread;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ClassUtils.isPresent;

/**
 * @author Steven Gentens
 * @since 3.0.0
 */
class TestBootstrapWithoutEntityModuleClassPath
{
	@Test
	void classesShouldNotBeOnTheClassPath() {
		assertThat( isPresent( "com.foreach.across.modules.entity.EntityModule", currentThread().getContextClassLoader() ) ).isFalse();
	}

	@Test
	void emptyBootstrap() {
		try (AcrossTestContext context = web( true )
				.modules( FileManagerModule.NAME )
				.build()) {
			assertThat( context.contextInfo().isBootstrapped() ).isTrue();
			assertThat( context.getBeansOfType( FileManager.class ) ).isNotEmpty();
			assertThat( context.findBeanOfTypeFromModule( FileManagerModule.NAME, FileReferenceInterceptor.class ) ).isEmpty();
			assertThat( context.findBeanOfTypeFromModule( FileManagerModule.NAME, MultipartFileToFileReferenceConverter.class ) ).isEmpty();
		}
	}

	@Test
	void bootstrapWithHibernateAndPropertiesModule() {
		try (AcrossTestContext context = web( true )
				.modules( FileManagerModule.NAME, PropertiesModule.NAME, AcrossHibernateJpaModule.NAME )
				.build()) {
			assertThat( context.contextInfo().isBootstrapped() ).isTrue();
			assertThat( context.getBeansOfType( FileManager.class ) ).isNotEmpty();
			assertThat( context.findBeanOfTypeFromModule( FileManagerModule.NAME, FileReferenceInterceptor.class ) ).isNotEmpty();
			assertThat( context.findBeanOfTypeFromModule( FileManagerModule.NAME, MultipartFileToFileReferenceConverter.class ) ).isEmpty();
		}
	}

}
