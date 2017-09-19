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

package modules.multidomaintest.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.across.core.installers.InstallerRunCondition;
import com.foreach.across.modules.webcms.installers.AbstractWebCmsDataInstaller;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * Imports a (realistic) set of reference data, only to be used for import verification.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Order(1)
@Installer(description = "Installs reference data", runCondition = InstallerRunCondition.AlwaysRun, phase = InstallerPhase.AfterModuleBootstrap)
public class ReferenceDataInstaller extends AbstractWebCmsDataInstaller
{
	@Override
	protected void registerResources( List<String> locations ) {
		locations.add( "classpath:installers/WebCmsModule/default-assets.yml" );

		// Apply base data import
		locations.add( "classpath:installers/multi-domain-reference-data/base-domains.yml" );
		locations.add( "classpath:installers/multi-domain-reference-data/base-types.yml" );
		locations.add( "classpath:installers/multi-domain-reference-data/base-menus.yml" );
		locations.add( "classpath:installers/multi-domain-reference-data/base-pages.yml" );
		locations.add( "classpath:installers/multi-domain-reference-data/base-publications.yml" );
		locations.add( "classpath:installers/multi-domain-reference-data/base-articles.yml" );
		locations.add( "classpath:installers/multi-domain-reference-data/base-components.yml" );

		// Apply base data imported as map-data
		locations.add( "classpath:installers/multi-domain-reference-data/map-be-foreach.yml" );
		locations.add( "classpath:installers/multi-domain-reference-data/map-nl-foreach.yml" );
		locations.add( "classpath:installers/multi-domain-reference-data/map-de-foreach.yml" );

		// Apply changes to base data
		locations.add( "classpath:installers/multi-domain-reference-data/extension-types.yml" );
		locations.add( "classpath:installers/multi-domain-reference-data/extension-pages.yml" );
		locations.add( "classpath:installers/multi-domain-reference-data/extension-map-be-foreach.yml" );
		locations.add( "classpath:installers/multi-domain-reference-data/extension-map-nl-foreach.yml" );
		locations.add( "classpath:installers/multi-domain-reference-data/extension-map-de-foreach.yml" );

		// Base data import with wcm:domain
		locations.add( "classpath:installers/multi-domain-reference-data/scope-based-file.yml" );
		locations.add( "classpath:installers/multi-domain-reference-data/scope-based-assets.yml" );
		locations.add( "classpath:installers/multi-domain-reference-data/scope-based-asset-type.yml" );
	}
}
