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

package com.foreach.across.modules.webcms.installers;

import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.modules.webcms.data.WebCmsDataImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Base installer class for importing data from one or more YAML resources.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
public abstract class AbstractWebCmsDataInstaller
{
	private ApplicationContext applicationContext;
	private WebCmsDataImportService dataImportService;

	private Resource[] resources = new Resource[0];

	/**
	 * Set one or more resources that represent YAML files that contain data that should be imported.
	 *
	 * @param resources to the resources
	 */
	public void setResources( Resource... resources ) {
		this.resources = resources;
	}

	/**
	 * Set one or more resources that represent YAML files that contain data that should be imported.
	 * Specify the resource locations, these will be resolved on the {@link ApplicationContext}.
	 * <p/>
	 * NOTE: can only be called after post construct or the applicationContext might not be set.
	 *
	 * @param locations to the resources
	 */
	public void setResources( String... locations ) {
		resources = new Resource[locations.length];
		for ( int i = 0; i < locations.length; i++ ) {
			resources[i] = applicationContext.getResource( locations[i] );
		}
	}

	@InstallerMethod
	@SuppressWarnings("unchecked")
	public void install() throws Exception {
		Yaml yaml = new Yaml();

		Stream.of( resources )
		      .forEach( resource -> {
			      try {
				      Map<String, Object> data = (Map<String, Object>) yaml.load( resource.getInputStream() );
				      dataImportService.importData( data );
			      }
			      catch ( IOException ioe ) {
				      throw new RuntimeException( ioe );
			      }
		      } );
	}

	@Autowired
	public void setApplicationContext( ApplicationContext applicationContext ) {
		this.applicationContext = applicationContext;
	}

	@Autowired
	public void setDataImportService( WebCmsDataImportService dataImportService ) {
		this.dataImportService = dataImportService;
	}
}
