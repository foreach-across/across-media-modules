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

package com.foreach.across.modules.webcms.data;

import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.across.modules.hibernate.unitofwork.UnitOfWork;
import com.foreach.across.modules.hibernate.unitofwork.UnitOfWorkFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Core API for importing data represented as a map into known assets.
 * <p/>
 * It is strongly advised to set a {@link UnitOfWorkFactory} on the service for managing persistence sessions.
 * If no {@link UnitOfWorkFactory} is configured, a global session might not be available in which case lazy loading will fail.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
@Slf4j
public final class WebCmsDataImportService
{
	private UnitOfWorkFactory unitOfWorkFactory;

	private Collection<WebCmsDataImporter> importers = Collections.emptyList();

	/**
	 * Import a collection of data.  The data is expected to be represented as a map containing other maps.
	 * The initial keys in the data map should be the root types.
	 *
	 * @param data containing everything that should be imported
	 */
	public void importData( Map<String, Object> data ) {
		data.forEach( ( key, value ) -> importData( new WebCmsDataEntry( key, value ) ) );
	}

	/**
	 * Import a data entry.  All registered {@link WebCmsDataImporter} beans will be checked to see if
	 * they support the given data set.
	 *
	 * @param data containing everything that should be imported
	 */
	public void importData( WebCmsDataEntry data ) {
		if ( unitOfWorkFactory != null ) {
			try (UnitOfWork ignore = unitOfWorkFactory.start()) {
				performImport( data );
			}
		}
		else {
			LOG.warn( "No UnitOfWorkFactory has been configured - strongly advised to have a UnitOfWorkFactory bean for data importing outside a web request" );
			performImport( data );
		}
	}

	private void performImport( WebCmsDataEntry data ) {
		importers.stream()
		         .filter( i -> i.supports( data ) )
		         .findFirst()
		         .orElseThrow( () -> new IllegalArgumentException( "Unable to import data " + data.getKey() ) )
		         .importData( data );
	}

	@Autowired
	void setImporters( @RefreshableCollection(includeModuleInternals = true, incremental = true) Collection<WebCmsDataImporter> importers ) {
		this.importers = importers;
	}

	@Autowired(required = false)
	public void setUnitOfWorkFactory( UnitOfWorkFactory unitOfWorkFactory ) {
		this.unitOfWorkFactory = unitOfWorkFactory;
	}
}
