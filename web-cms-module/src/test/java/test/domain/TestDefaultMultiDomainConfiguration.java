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

package test.domain;

import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.webcms.WebCmsModule;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainBound;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.across.test.AcrossWebAppConfiguration;
import it.DynamicDataSourceConfigurer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
@RunWith(SpringJUnit4ClassRunner.class)
@AcrossWebAppConfiguration
public class TestDefaultMultiDomainConfiguration extends AbstractMultiDomainTest
{
	@Test
	public void domainPropertyShouldBeHidden() {
		entityRegistry.getEntities()
		              .stream()
		              .filter( cfg -> WebCmsDomainBound.class.isAssignableFrom( cfg.getEntityType() ) )
		              .map( cfg -> cfg.getPropertyRegistry().getProperty( "domain" ) )
		              .forEach( prop -> {
			              assertFalse( prop.isReadable() );
			              assertTrue( prop.isWritable() );
			              assertTrue( prop.isHidden() );
		              } );
	}

	@AcrossTestConfiguration(modules = { AdminWebModule.NAME, EntityModule.NAME, WebCmsModule.NAME })
	@Configuration
	protected static class Config extends DynamicDataSourceConfigurer
	{
	}
}
