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

package modules.multidomaintest.config;

import com.foreach.across.modules.webcms.domain.domain.web.WebCmsDomainLocaleContextResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
@Configuration
public class DefaultLocaleConfiguration
{
	@Autowired
	public void setGlobalDefaultLocale( WebCmsDomainLocaleContextResolver localeContextResolver ) {
		localeContextResolver.setDefaultLocale( Locale.ITALY );
	}
}
