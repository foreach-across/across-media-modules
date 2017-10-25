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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static com.foreach.across.modules.webcms.data.WebCmsDataAction.CREATE;
import static com.foreach.across.modules.webcms.data.WebCmsDataAction.UPDATE;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAbstractWebCmsDataImporter
{
	@Mock
	private WebCmsDataConversionService conversionService;

	@Mock
	private WebCmsPropertyDataImporter<String> before;

	@Mock
	private WebCmsPropertyDataImporter<String> after;

	@Spy
	private AbstractWebCmsDataImporter<String, String> importer;

	private Map<String, Object> values = Collections.singletonMap( "my", Collections.singletonMap( "sub", "value" ) );
	private WebCmsDataEntry data = WebCmsDataEntry.builder().key( "data" ).data( values ).build();

	@Before
	public void setUp() throws Exception {
		importer.setConversionService( conversionService );
		WebCmsPropertyDataImportService propertyDataImportService = new WebCmsPropertyDataImportService();
		propertyDataImportService.setPropertyDataImporters( Arrays.asList( before, after ) );
		importer.setPropertyDataImportService( propertyDataImportService );

		when( conversionService.convertToPropertyValues( any(), any() ) ).thenReturn( true );

		when( before.supports( eq( WebCmsPropertyDataImporter.Phase.BEFORE_ASSET_SAVED ), anyString(), anyBoolean(), any() ) ).thenReturn( true );
		when( after.supports( eq( WebCmsPropertyDataImporter.Phase.AFTER_ASSET_SAVED ), anyString(), anyBoolean(), any() ) ).thenReturn( true );
	}

	@Test
	public void deleteShouldBeIgnoredIfNoExistingInstance() {
		expectIgnoredIfNotExists( WebCmsDataImportAction.DELETE );
	}

	@Test
	public void deleteIfExistingInstance() {
		data.setImportAction( WebCmsDataImportAction.DELETE );

		when( importer.retrieveExistingInstance( data ) ).thenReturn( "one" );
		importer.importData( data );

		verify( importer, never() ).saveDto( any(), any(), any() );
		verify( importer ).deleteInstance( "one", data );
	}

	@Test
	public void updateShouldBeIgnoredIfNoExistingInstance() {
		expectIgnoredIfNotExists( WebCmsDataImportAction.UPDATE );
	}

	@Test
	public void updateUpdatesExistingInstance() {
		expectExistingUpdated( WebCmsDataImportAction.UPDATE, UPDATE );
	}

	@Test
	public void createIsOnlyCalledIfNoExistingInstance() {
		expectCreationIfNotExists( WebCmsDataImportAction.CREATE );
	}

	@Test
	public void createIsIgnoredIfExistingInstance() {
		expectExistingIgnored( WebCmsDataImportAction.CREATE );
	}

	@Test
	public void createOrUpdateCreatesIfNoExistingInstance() {
		expectCreationIfNotExists( WebCmsDataImportAction.CREATE_OR_UPDATE );
	}

	@Test
	public void createOrUpdateUpdatesIfNoExistingInstance() {
		expectExistingUpdated( WebCmsDataImportAction.CREATE_OR_UPDATE, UPDATE );
	}

	@Test
	public void createOrReplaceCreatesIfNoExistingInstance() {
		expectCreationIfNotExists( WebCmsDataImportAction.CREATE_OR_REPLACE );
	}

	@Test
	public void createOrReplaceReplacesIfExistingInstance() {
		expectExistingUpdated( WebCmsDataImportAction.CREATE_OR_REPLACE, WebCmsDataAction.REPLACE );
	}

	@Test
	public void replaceIgnoredIfNoExistingInstance() {
		expectIgnoredIfNotExists( WebCmsDataImportAction.REPLACE );
	}

	@Test
	public void replaceReplacesIfExistingInstance() {
		expectExistingUpdated( WebCmsDataImportAction.REPLACE, WebCmsDataAction.REPLACE );
	}

	@Test
	public void saveIsAlwaysTriggeredForNewInstance() {
		data.setImportAction( WebCmsDataImportAction.CREATE );

		when( importer.createDto( data, null, CREATE, data.getMapData() ) ).thenReturn( "create" );
		when( conversionService.convertToPropertyValues( values, "create" ) ).thenReturn( false );

		importer.importData( data );

		verify( before ).importData( WebCmsPropertyDataImporter.Phase.BEFORE_ASSET_SAVED, WebCmsDataEntry.builder().key( "my" )
		                                                                                                 .parent( data )
		                                                                                                 .data( Collections.singletonMap( "sub", "value" ) )
		                                                                                                 .build(), "create", CREATE );
		verify( after ).importData( WebCmsPropertyDataImporter.Phase.AFTER_ASSET_SAVED, WebCmsDataEntry.builder().key( "my" )
		                                                                                               .parent( data )
		                                                                                               .data( Collections.singletonMap( "sub", "value" ) )
		                                                                                               .build(), "create", CREATE );
		verify( importer ).saveDto( "create", CREATE, data );
	}

	@Test
	public void saveIsSkippedIfNoDataValuesApplied() {
		data.setImportAction( WebCmsDataImportAction.UPDATE );

		when( importer.retrieveExistingInstance( data ) ).thenReturn( "one" );
		when( importer.createDto( data, "one", UPDATE, data.getMapData() ) ).thenReturn( "updated" );
		when( conversionService.convertToPropertyValues( values, "updated" ) ).thenReturn( false );

		importer.importData( data );

		verify( before ).importData( WebCmsPropertyDataImporter.Phase.BEFORE_ASSET_SAVED, WebCmsDataEntry.builder().key( "my" )
		                                                                                                 .parent( data )
		                                                                                                 .data( Collections.singletonMap( "sub", "value" ) )
		                                                                                                 .build(),
		                             "updated",
		                             UPDATE );
		verify( after ).importData( WebCmsPropertyDataImporter.Phase.AFTER_ASSET_SAVED, WebCmsDataEntry.builder().key( "my" )
		                                                                                               .parent( data )
		                                                                                               .data( Collections.singletonMap( "sub", "value" ) )
		                                                                                               .build(), "updated", UPDATE );
		verify( importer, never() ).saveDto( "updated", UPDATE, data );
	}

	@Test
	public void saveTriggeredIfDataImported() {
		data.setImportAction( WebCmsDataImportAction.UPDATE );

		when( importer.retrieveExistingInstance( data ) ).thenReturn( "one" );
		when( importer.createDto( data, "one", UPDATE, data.getMapData() ) ).thenReturn( "updated" );
		when( conversionService.convertToPropertyValues( values, "updated" ) ).thenReturn( false );
		when( before.importData( any(), any(), any(), any() ) ).thenReturn( true );

		importer.importData( data );

		verify( before ).importData( WebCmsPropertyDataImporter.Phase.BEFORE_ASSET_SAVED, WebCmsDataEntry.builder().key( "my" )
		                                                                                                 .parent( data )
		                                                                                                 .data( Collections.singletonMap( "sub", "value" ) )
		                                                                                                 .build(), "updated",
		                             UPDATE );
		verify( after ).importData( WebCmsPropertyDataImporter.Phase.AFTER_ASSET_SAVED, WebCmsDataEntry.builder().key( "my" )
		                                                                                               .parent( data )
		                                                                                               .data( Collections.singletonMap( "sub", "value" ) )
		                                                                                               .build(), "updated", UPDATE );
		verify( importer ).saveDto( "updated", UPDATE, data );
	}

	private void expectCreationIfNotExists( WebCmsDataImportAction importAction ) {
		data.setImportAction( importAction );

		when( importer.createDto( data, null, CREATE, data.getMapData() ) ).thenReturn( "create" );
		importer.importData( data );

		verify( before ).importData( WebCmsPropertyDataImporter.Phase.BEFORE_ASSET_SAVED, WebCmsDataEntry.builder().key( "my" )
		                                                                                                 .parent( data )
		                                                                                                 .data( Collections.singletonMap( "sub", "value" ) )
		                                                                                                 .build(), "create", CREATE );
		verify( after ).importData( WebCmsPropertyDataImporter.Phase.AFTER_ASSET_SAVED, WebCmsDataEntry.builder().key( "my" )
		                                                                                               .parent( data )
		                                                                                               .data( Collections.singletonMap( "sub", "value" ) )
		                                                                                               .build(), "create", CREATE );
		verify( conversionService ).convertToPropertyValues( values, "create" );
		verify( importer ).saveDto( "create", CREATE, data );
	}

	private void expectExistingUpdated( WebCmsDataImportAction importAction, WebCmsDataAction action ) {
		data.setImportAction( importAction );

		when( importer.retrieveExistingInstance( data ) ).thenReturn( "one" );
		when( importer.createDto( data, "one", action, data.getMapData() ) ).thenReturn( "updated" );

		importer.importData( data );

		verify( before ).importData( WebCmsPropertyDataImporter.Phase.BEFORE_ASSET_SAVED, WebCmsDataEntry.builder().key( "my" )
		                                                                                                 .parent( data )
		                                                                                                 .data( Collections.singletonMap( "sub", "value" ) )
		                                                                                                 .build(), "updated",
		                             action );
		verify( after ).importData( WebCmsPropertyDataImporter.Phase.AFTER_ASSET_SAVED, WebCmsDataEntry.builder().key( "my" )
		                                                                                               .parent( data )
		                                                                                               .data( Collections.singletonMap( "sub", "value" ) )
		                                                                                               .build(), "updated", action );
		verify( conversionService ).convertToPropertyValues( values, "updated" );
		verify( importer ).saveDto( "updated", action, data );
	}

	private void expectExistingIgnored( WebCmsDataImportAction importAction ) {
		data.setImportAction( importAction );

		when( importer.retrieveExistingInstance( data ) ).thenReturn( "one" );

		importer.importData( data );
		verify( importer, never() ).saveDto( any(), any(), any() );
		verify( importer, never() ).deleteInstance( any(), any() );
	}

	private void expectIgnoredIfNotExists( WebCmsDataImportAction importAction ) {
		data.setImportAction( importAction );
		importer.importData( data );
		verify( importer, never() ).saveDto( any(), any(), any() );
		verify( importer, never() ).deleteInstance( any(), any() );
	}
}
