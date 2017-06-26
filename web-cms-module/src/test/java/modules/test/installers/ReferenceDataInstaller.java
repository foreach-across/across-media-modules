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

package modules.test.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.across.core.installers.InstallerRunCondition;
import com.foreach.across.modules.webcms.installers.AbstractWebCmsDataInstaller;

import java.util.List;

/**
 * Imports a (realistic) set of reference data, only to be used for import verification.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Installer(description = "Installs reference data", runCondition = InstallerRunCondition.AlwaysRun, phase = InstallerPhase.AfterModuleBootstrap)
public class ReferenceDataInstaller extends AbstractWebCmsDataInstaller
{
	@Override
	protected void registerResources( List<String> locations ) {
		// Apply base data import
		locations.add( "classpath:installers/reference-data/base-menus.yml" );
		locations.add( "classpath:installers/reference-data/base-pages.yml" );

		// Apply extensions that modify previously imported base data
		locations.add( "classpath:installers/reference-data/extension-menus.yml" );
		locations.add( "classpath:installers/reference-data/extension-pages.yml" );
	}
}
