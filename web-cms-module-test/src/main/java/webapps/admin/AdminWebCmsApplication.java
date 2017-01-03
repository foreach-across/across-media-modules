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

package webapps.admin;

import com.foreach.across.config.AcrossApplication;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.user.UserModule;
import com.foreach.across.modules.user.UserModuleSettings;
import com.foreach.across.modules.webcms.WebCmsModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.h2.H2ConsoleAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

/**
 * Main application for a website with the administration UI active, allowing dynamic addition of pages.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@AcrossApplication(
		modules = {
				WebCmsModule.NAME,
				EntityModule.NAME,
				AdminWebModule.NAME
		}
)
@Import({ DataSourceAutoConfiguration.class, H2ConsoleAutoConfiguration.class })
public class AdminWebCmsApplication
{
	@Bean
	public UserModule userModule() {
		UserModule userModule = new UserModule();
		userModule.setProperty( UserModuleSettings.PASSWORD_ENCODER, NoOpPasswordEncoder.getInstance() );
		return userModule;
	}

	public static void main( String[] args ) {
		SpringApplication.run( AdminWebCmsApplication.class, args );
	}
}
