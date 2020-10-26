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

package com.foreach.across.modules.webcms.domain.domain;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
public class TestWebCmsDomain
{
	@Test
	public void collectionIdValueShouldBeFixed() {
		assertEquals( "wcm:domain", WebCmsDomain.COLLECTION_ID );
	}

	@Test
	public void defaultValues() {
		WebCmsDomain domain = new WebCmsDomain();
		verifyDefaultValues( domain );
		verifyDefaultValues( WebCmsDomain.builder().build() );
		verifyDefaultValues( domain.toDto() );
		verifyDefaultValues( domain.toBuilder().build() );
	}

	private void verifyDefaultValues( WebCmsDomain domain ) {
		assertNull( domain.getId() );
		assertNull( domain.getNewEntityId() );
		assertTrue( domain.isNew() );
		assertNull( domain.getObjectId() );
		assertNull( domain.getDomainKey() );
		assertNull( domain.getName() );
		assertNull( domain.getCreatedBy() );
		assertNull( domain.getCreatedDate() );
		assertNull( domain.getLastModifiedBy() );
		assertNull( domain.getLastModifiedDate() );

		assertNull( domain.getDescription() );
		assertTrue( domain.getAttributes().isEmpty() );
		assertTrue( domain.isActive() );
	}

	@Test
	public void builderSemantics() {
		Date timestamp = new Date();

		WebCmsDomain domain = WebCmsDomain.builder()
		                                  .newEntityId( 123L )
		                                  .domainKey( "domain-key" )
		                                  .name( "my-domain" )
		                                  .description( "my-description" )
		                                  .createdBy( "john" )
		                                  .createdDate( timestamp )
		                                  .lastModifiedBy( "josh" )
		                                  .attribute( "profile", "test" )
		                                  .build();

		assertNull( domain.getId() );
		assertEquals( Long.valueOf( 123L ), domain.getNewEntityId() );
		assertEquals( "wcm:domain:domain-key", domain.getObjectId() );
		assertEquals( "domain-key", domain.getDomainKey() );
		assertEquals( "my-domain", domain.getName() );
		assertEquals( "my-description", domain.getDescription() );
		assertEquals( "john", domain.getCreatedBy() );
		assertEquals( timestamp, domain.getCreatedDate() );
		assertEquals( "josh", domain.getLastModifiedBy() );
		assertEquals( "test", domain.getAttributes().get( "profile" ) );
		assertNull( domain.getLastModifiedDate() );
		assertTrue( domain.isActive() );

		WebCmsDomain other = domain.toBuilder()
		                           .id( 333L )
		                           .objectId( "my-type" )
		                           .lastModifiedDate( timestamp )
		                           .active( false )
		                           .build();
		assertNotSame( domain, other );

		assertNull( other.getNewEntityId() );
		assertEquals( Long.valueOf( 333L ), other.getId() );
		assertEquals( "wcm:domain:my-type", other.getObjectId() );
		assertEquals( "domain-key", domain.getDomainKey() );
		assertEquals( "my-domain", domain.getName() );
		assertEquals( "my-description", domain.getDescription() );
		assertEquals( "john", other.getCreatedBy() );
		assertEquals( timestamp, other.getCreatedDate() );
		assertEquals( "josh", other.getLastModifiedBy() );
		assertEquals( "test", domain.getAttributes().get( "profile" ) );
		assertEquals( timestamp, other.getLastModifiedDate() );
		assertFalse( other.isActive() );
	}
}
