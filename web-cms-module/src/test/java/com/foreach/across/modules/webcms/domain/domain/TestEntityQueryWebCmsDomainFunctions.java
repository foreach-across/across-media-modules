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

import com.foreach.across.modules.entity.query.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
public class TestEntityQueryWebCmsDomainFunctions
{
	private WebCmsDomain one = WebCmsDomain.builder().id( 1L ).build();
	private WebCmsDomain two = WebCmsDomain.builder().id( 2L ).build();

	private EntityQueryConditionTranslator translator = EntityQueryWebCmsDomainFunctions.conditionTranslator();

	@Test
	public void unmodifiedQuery() {
		EntityQueryCondition q = new EntityQueryCondition( "domain", EntityQueryOps.IS_NOT_NULL );
		assertSame( q, translator.translate( q ) );
	}

	@Test
	public void equalsNullDomainOnly() {
		EntityQueryCondition q = new EntityQueryCondition( "domain", EntityQueryOps.EQ, new Object[] { null } );
		EntityQueryCondition expected = new EntityQueryCondition( "domain", EntityQueryOps.IS_NULL );
		assertEquals( expected, translator.translate( q ) );
	}

	@Test
	public void notEqualToNullDomain() {
		EntityQueryCondition q = new EntityQueryCondition( "domain", EntityQueryOps.NEQ, new Object[] { null } );
		EntityQueryCondition expected = new EntityQueryCondition( "domain", EntityQueryOps.IS_NOT_NULL );
		assertEquals( expected, translator.translate( q ) );
	}

	@Test
	public void equalsToDomain() {
		EntityQueryCondition q = new EntityQueryCondition( "domain", EntityQueryOps.EQ, one );
		EntityQueryCondition expected = new EntityQueryCondition( "domain", EntityQueryOps.EQ, one );
		assertEquals( expected, translator.translate( q ) );
	}

	@Test
	public void notEqualToDomain() {
		EntityQueryCondition q = new EntityQueryCondition( "domain", EntityQueryOps.NEQ, one );
		EntityQueryCondition expected = new EntityQueryCondition( "domain", EntityQueryOps.NEQ, one );
		assertEquals( expected, translator.translate( q ) );
	}

	@Test
	public void equalsToMultipleDomains() {
		EntityQueryCondition q = new EntityQueryCondition( "domain", EntityQueryOps.EQ, one, two );
		EntityQueryCondition expected = new EntityQueryCondition( "domain", EntityQueryOps.IN, one, two );
		assertEquals( expected, translator.translate( q ) );
	}

	@Test
	public void notEqualToMultipleDomains() {
		EntityQueryCondition q = new EntityQueryCondition( "domain", EntityQueryOps.NEQ, one, two );
		EntityQueryCondition expected = new EntityQueryCondition( "domain", EntityQueryOps.NOT_IN, one, two );
		assertEquals( expected, translator.translate( q ) );
	}

	@Test
	public void equalsToMultipleDomainsAndNull() {
		EntityQueryCondition q = new EntityQueryCondition( "domain", EntityQueryOps.EQ, one, null, two );
		EntityQueryExpression expected =
				EntityQuery.or(
						new EntityQueryCondition( "domain", EntityQueryOps.IN, one, two ),
						new EntityQueryCondition( "domain", EntityQueryOps.IS_NULL )

				);
		assertEquals( expected, translator.translate( q ) );
	}

	@Test
	public void notEqualToMultipleDomainsAndNull() {
		EntityQueryCondition q = new EntityQueryCondition( "domain", EntityQueryOps.NEQ, one, null, two );
		EntityQueryExpression expected =
				EntityQuery.and(
						new EntityQueryCondition( "domain", EntityQueryOps.NOT_IN, one, two ),
						new EntityQueryCondition( "domain", EntityQueryOps.IS_NOT_NULL )

				);
		assertEquals( expected, translator.translate( q ) );
	}
}
