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

package test.domain;

import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.query.EntityQueryConditionTranslator;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.EntityRegistry;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.webcms.domain.domain.EntityQueryWebCmsDomainFunctions;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainBound;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
public abstract class AbstractMultiDomainTest
{
	@Autowired
	protected EntityRegistry entityRegistry;

	@Test
	public void domainEntityShouldBeHidden() {
		assertTrue( entityRegistry.getEntityConfiguration( WebCmsDomain.class ).isHidden() );
	}

	@Test
	public void domainConditionTranslatorShouldBeRegisteredOnIdPropertyOfDomain() {
		EntityConfiguration config = entityRegistry.getEntityConfiguration( WebCmsDomain.class );
		assertEquals(
				EntityQueryWebCmsDomainFunctions.conditionTranslator(),
				config.getPropertyRegistry().getProperty( "id" ).getAttribute( EntityQueryConditionTranslator.class )
		);
	}

	@Test
	public void domainConditionTranslatorShouldBeRegisteredOnAllDomainPropertiesOfWebCmsDomainBound() {
		entityRegistry.getEntities()
		              .stream()
		              .filter( cfg -> WebCmsDomainBound.class.isAssignableFrom( cfg.getEntityType() ) )
		              .map( cfg -> cfg.getPropertyRegistry().getProperty( "domain" ) )
		              .forEach( prop ->
				                        assertEquals(
						                        EntityQueryWebCmsDomainFunctions.conditionTranslator(),
						                        prop.getAttribute( EntityQueryConditionTranslator.class )
				                        )
		              );
	}

	protected void assertOptionQuery( Class<?> type, String query ) {
		EntityConfiguration config = entityRegistry.getEntityConfiguration( type );
		assertEquals( query, config.getAttribute( EntityAttributes.OPTIONS_ENTITY_QUERY ) );
	}

	protected void assertDomainPropertyAvailable( Class<?> entityType ) {
		EntityPropertyDescriptor prop = entityRegistry.getEntityConfiguration( entityType )
		                                              .getPropertyRegistry()
		                                              .getProperty( "domain" );

		assertTrue( prop.isReadable() );
		assertTrue( prop.isWritable() );
		assertFalse( prop.isHidden() );
	}

	protected void assertDomainPropertyHidden( Class<?> entityType ) {
		EntityPropertyDescriptor prop = entityRegistry.getEntityConfiguration( entityType )
		                                              .getPropertyRegistry()
		                                              .getProperty( "domain" );

		assertFalse( prop.isReadable() );
		assertTrue( prop.isWritable() );
		assertTrue( prop.isHidden() );
	}

	protected void assertDomainPropertyDisabled( Class<?> entityType ) {
		EntityPropertyDescriptor prop = entityRegistry.getEntityConfiguration( entityType )
		                                              .getPropertyRegistry()
		                                              .getProperty( "domain" );

		assertFalse( prop.isReadable() );
		assertFalse( prop.isWritable() );
		assertTrue( prop.isHidden() );
	}
}
