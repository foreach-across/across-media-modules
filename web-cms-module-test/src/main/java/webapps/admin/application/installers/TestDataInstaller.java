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

package webapps.admin.application.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.across.modules.webcms.installers.AbstractWebCmsDataInstaller;
import lombok.RequiredArgsConstructor;

import javax.annotation.PostConstruct;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Installer(description = "Install some test data", phase = InstallerPhase.AfterModuleBootstrap, version = 5)
@RequiredArgsConstructor
public class TestDataInstaller extends AbstractWebCmsDataInstaller
{
	@PostConstruct
	public void registerResources() {
		setResources(
				"classpath:installers/test-data/articles.yml",
				"classpath:installers/test-data/pages.yml"
		);
	}
}
