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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Base installer class for importing data from one or more YAML resources.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Slf4j
public abstract class AbstractWebCmsDataInstaller
{
	private ApplicationContext applicationContext;
	private WebCmsDataImportService dataImportService;

	private Collection<Resource> resources = new ArrayList<>();

	@PostConstruct
	final void autoRegisterResources() {
		List<String> list = new ArrayList<>();
		registerResources( list );

		list.forEach( this::addResource );
	}

	/**
	 * Add a single YAML resource to import by specifying the resource location.
	 *
	 * @param location of the resource
	 */
	public void addResource( String location ) {
		addResource( applicationContext.getResource( location ) );
	}

	/**
	 * Add a single YAML resource to import.
	 *
	 * @param resource to add
	 */
	public void addResource( Resource resource ) {
		resources.add( resource );
	}

	/**
	 * Register YAML resources to import.
	 * Will be called after the installer bean has been constructed.
	 *
	 * @param locations to add
	 */
	protected abstract void registerResources( List<String> locations );

	@InstallerMethod
	@SuppressWarnings("unchecked")
	public void install() throws Exception {
		Yaml yaml = new Yaml();

		resources
				.forEach( resource -> {
					try {
						LOG.info( "Importing YAML data from: " + resource );
						Map<String, Object> data = (Map<String, Object>) yaml.load( resource.getInputStream() );
						dataImportService.importData( data, resource.toString() );
						LOG.info( "Finished importing YAML data from: " + resource );
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
