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

package modules.multidomaintest;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import com.foreach.across.modules.webcms.WebCmsModule;
import modules.multidomaintest.config.DefaultLocaleConfiguration;
import modules.multidomaintest.controllers.DomainController;

import java.util.Set;

@AcrossDepends(required = WebCmsModule.NAME)
public class CmsMultiDomainTestModule extends AcrossModule
{
	@Override
	public String getName() {
		return "CmsMultiDomainTestModule";
	}

	@Override
	public String getResourcesKey() {
		return "multidomaintest";
	}

	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		contextConfigurers.add( new ComponentScanConfigurer( DomainController.class, DefaultLocaleConfiguration.class ) );
	}
}
