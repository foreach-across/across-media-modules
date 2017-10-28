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

package it;

import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import com.foreach.across.modules.webcms.data.WebCmsDataImportException;
import com.foreach.across.modules.webcms.data.WebCmsDataImportService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
@Slf4j
public class ITDataImportExceptions extends AbstractCmsApplicationIT
{
	@Autowired
	private WebCmsDataImportService dataImportService;

	@Test
	public void exceptionToString() {
		WebCmsDataEntry data = WebCmsDataEntry.builder()
		                                      .identifier( "my-data" )
		                                      .key( "mykey" )
		                                      .data( map( "one", "two" ) )
		                                      .build();
		String dataToString = data.toString();

		WebCmsDataImportException ie = new WebCmsDataImportException( data );
		assertEquals( "Failed to import data: " + dataToString, ie.getMessage() );
	}

	@Ignore
	@Test
	public void unableToImportDataWithKey() {
		WebCmsDataImportException e = importData(
				"bad-data",
				map( "unknown-data-key", list() )
		);

		WebCmsDataEntry data = e.getDataEntry();
		assertNotNull( data );
		assertEquals( "bad-data", data.getIdentifier() );
		assertEquals( "/root/unknown-data-key", data.getLocation() );

		assertTrue( e.getCause() instanceof IllegalArgumentException );
		assertEquals( "Unable to import data for key: unknown-data-key", e.getCause().getMessage() );
	}

	@Test
	public void invalidObjectIdException() {
		WebCmsDataImportException e = importData(
				"invalidObjectIdException",
				map( "assets", map( "article", list( map( "objectId", "badObjectId" ) ) ) )
		);

		WebCmsDataEntry data = e.getDataEntry();
		assertNotNull( data );
		assertEquals( "invalidObjectIdException", data.getIdentifier() );
		assertEquals( "/assets/article/<map>", data.getLocation() );
		assertEquals( map( "objectId", "badObjectId" ), data.getMapData() );

		assertTrue( e.getCause() instanceof IllegalArgumentException );
	}

	@Test
	public void validationFailedException() {
		WebCmsDataImportException e = importData(
				"validation-fails",
				map( "types", map( "article", list( map( "description", "Invalid article type..." ) ) ) )
		);
	}

	@Test
	public void invalidPropertyConversionException() {
		WebCmsDataImportException e = importData(
				"invalid-property-value",
				map( "assets", map( "article", list( map( "articleType", "this-type-does-not-exist" ) ) ) )
		);

		WebCmsDataEntry data = e.getDataEntry();
		assertNotNull( data );
		assertEquals( "invalid-property-value", data.getIdentifier() );
		assertEquals( "/assets/article/<map>", data.getLocation() );
		assertEquals( map( "articleType", "this-type-does-not-exist" ), data.getMapData() );

		assertTrue( e.getCause() instanceof IllegalArgumentException );
	}

	private WebCmsDataImportException importData( String identifier, Map<String, Object> data ) {
		try {
			dataImportService.importData( data, identifier );
		}
		catch ( WebCmsDataImportException ie ) {
			LOG.error( "Caught expected WebCmsDataImportException", ie );
			return ie;
		}
		catch ( Exception e ) {
			fail( "Wrong exception caught: " + e );
		}

		throw new AssertionError( "Expected a WebCmsDataImportException to be thrown" );
	}

	private List<Object> list( Object... values ) {
		return Arrays.asList( values );
	}

	private Map<String, Object> map( Object... keyOrValue ) {
		Map<String, Object> data = new LinkedHashMap<>();

		for ( int i = 0; i < keyOrValue.length; i += 2 ) {
			data.put( (String) keyOrValue[i], keyOrValue[i + 1] );
		}
		return data;
	}
}
