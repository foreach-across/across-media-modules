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

package com.foreach.across.modules.webcms.domain.url;

import org.hibernate.engine.spi.SessionImplementor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Sander Van Loock
 * @since 0.0.1
 */
@RunWith(MockitoJUnitRunner.class)
public class TestHttpStatusType
{
	@Mock
	private PreparedStatement st;

	@Mock
	private Object value;

	private int index = 5;

	@Mock
	private SessionImplementor session;

	private HttpStatusType httpStatusType;
	@Mock
	private ResultSet rs;

	@Mock
	private Object owner;

	@Before
	public void setUp() throws Exception {
		httpStatusType = new HttpStatusType();
	}

	@Test
	public void nullSafeGet() throws Exception {
		HttpStatus expectedStatus = HttpStatus.OK;
		when( rs.getInt( "http_status" ) ).thenReturn( expectedStatus.value() );
		Object actual = httpStatusType.nullSafeGet( rs, new String[] { "http_status" }, session, owner );

		assertEquals( expectedStatus, actual );
		verify( rs, times( 1 ) ).getInt( anyString() );
		verifyNoMoreInteractions( rs );
		verifyZeroInteractions( session, owner );
	}

	@Test
	public void invalidStatusReturnsNull() throws Exception {
		when( rs.getInt( "http_status" ) ).thenReturn( 9999 );
		Object actual = httpStatusType.nullSafeGet( rs, new String[] { "http_status" }, session, owner );

		assertNull( actual );
		verify( rs, times( 1 ) ).getInt( anyString() );
		verifyNoMoreInteractions( rs );
		verifyZeroInteractions( session, owner );
	}

	@Test
	public void nullSafeSet() throws Exception {
		HttpStatus expectedStatus = HttpStatus.I_AM_A_TEAPOT;
		httpStatusType.nullSafeSet( st, expectedStatus, index, session );

		verify( st, times( 1 ) ).setInt( index, expectedStatus.value() );
		verifyNoMoreInteractions( st );
		verifyZeroInteractions( session );
	}

	@Test
	public void invalidValueSetsNull() throws Exception {
		httpStatusType.nullSafeSet( st, "InvalidValue", index, session );

		verify( st, times( 1 ) ).setNull( index, Types.INTEGER );
		verifyNoMoreInteractions( st );
		verifyZeroInteractions( session );
	}

	@Test
	public void invalidStatusCodeSetsNull() throws Exception {
		httpStatusType.nullSafeSet( st, 999, index, session );

		verify( st, times( 1 ) ).setNull( index, Types.INTEGER );
		verifyNoMoreInteractions( st );
		verifyZeroInteractions( session );
	}

}