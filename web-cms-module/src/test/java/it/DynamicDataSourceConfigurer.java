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

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.installers.InstallerAction;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import javax.sql.DataSource;

/**
 * Sets a data source with a unique name to avoid dirty context problems.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DynamicDataSourceConfigurer implements AcrossContextConfigurer
{
	@Override
	public void configure( AcrossContext context ) {
		context.setDataSource( acrossDataSource() );
		context.setInstallerAction( InstallerAction.EXECUTE );
	}

	@Bean
	public DataSource acrossDataSource() {
		return new EmbeddedDatabaseBuilder().generateUniqueName( true ).build();
	}
}