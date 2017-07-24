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

import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.webcms.WebCmsModule;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.across.test.AcrossWebAppConfiguration;
import org.junit.runner.RunWith;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Bootstraps simple application with WebCmsModule.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@AcrossWebAppConfiguration
@TestPropertySource(properties = "acrossHibernate.createUnitOfWorkFactory=true")
public abstract class AbstractCmsApplicationIT
{
	@AcrossTestConfiguration(modules = { WebCmsModule.NAME, AcrossHibernateJpaModule.NAME })
	protected static class Config
	{
	}
}
