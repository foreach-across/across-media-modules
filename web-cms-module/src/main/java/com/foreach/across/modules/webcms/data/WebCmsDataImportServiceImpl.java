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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Core API for importing data represented as a map into known assets.
 * Data imports are transactional and use the default {@link org.springframework.transaction.PlatformTransactionManager}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
@Slf4j
public final class WebCmsDataImportServiceImpl implements WebCmsDataImportService
{
	private Collection<WebCmsDataImporter> importers = Collections.emptyList();

	@Transactional
	@Override
	public void importData( Map<String, Object> data ) {
		importData( data, "generated:" + UUID.randomUUID().toString() );
	}

	@Transactional
	@Override
	public void importData( Map<String, Object> data, String identifier ) {
		importData( new WebCmsDataEntry( identifier, WebCmsDataEntry.ROOT, data ) );
	}

	@Transactional
	@Override
	public void importData( WebCmsDataEntry data ) {
		try {
			importers.stream()
			         .filter( i -> i.supports( data ) )
			         .findFirst()
			         .orElseThrow( () -> new IllegalArgumentException( "Unable to import data for key: " + data.getKey() ) )
			         .importData( data );
		}
		catch ( WebCmsDataImportException die ) {
			throw die;
		}
		catch ( Exception e ) {
			throw new WebCmsDataImportException( data, e );
		}
		finally {
			data.getCompletedCallbacks().forEach( callback -> callback.accept( data ) );
		}
	}

	@Autowired
	void setImporters( @RefreshableCollection(includeModuleInternals = true, incremental = true) Collection<WebCmsDataImporter> importers ) {
		this.importers = importers;
	}
}
