/*
 * Copyright 2017 the original author or authors
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

import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.webcms.WebCmsModule;
import com.foreach.across.modules.webcms.domain.domain.config.WebCmsMultiDomainConfiguration;
import com.foreach.across.modules.webcms.domain.domain.web.AbstractWebCmsDomainContextFilter;
import com.foreach.across.test.AcrossTestContext;
import com.foreach.across.test.AcrossTestWebContext;
import org.junit.jupiter.api.Test;

import static com.foreach.across.test.support.AcrossTestBuilders.web;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
class ITWebCmsModule
{
	@Test
	void noAdminWebModule() {
		try (AcrossTestWebContext ctx = web().modules( WebCmsModule.NAME, AcrossHibernateJpaModule.NAME )
		                                     .build()) {
			assertTrue( ctx.contextInfo().hasModule( WebCmsModule.NAME ) );

			assertMultiDomainConfigurationIsPresent( ctx );
			assertNull( ctx.getServletContext().getFilterRegistration( AbstractWebCmsDomainContextFilter.FILTER_NAME ) );
		}
	}

	private void assertMultiDomainConfigurationIsPresent( AcrossTestContext ctx ) {
		WebCmsMultiDomainConfiguration config = ctx.getBeanOfType( WebCmsMultiDomainConfiguration.class );
		assertNotNull( config );
		assertEquals( WebCmsMultiDomainConfiguration.disabled().build(), config );
	}

	@Test
	void adminWebWithoutEntityModule() {
		try (AcrossTestContext ctx = web().modules( WebCmsModule.NAME, AcrossHibernateJpaModule.NAME,
		                                            AdminWebModule.NAME )
		                                  .build()
		) {
			assertTrue( ctx.contextInfo().hasModule( WebCmsModule.NAME ) );
		}
	}

	@Test
	void entityAndAdminWebModule() {
		try (AcrossTestContext ctx = web().modules( WebCmsModule.NAME, AcrossHibernateJpaModule.NAME,
		                                            AdminWebModule.NAME, EntityModule.NAME )
		                                  .build()
		) {
			assertTrue( ctx.contextInfo().hasModule( WebCmsModule.NAME ) );
		}
	}
}
