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

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

/**
 * A custom Hibernate type to persist {@link HttpStatus} objects with their status code.
 *
 * @author Sander Van Loock
 * @since 0.0.1
 * @see HttpStatus#value
 */
@Slf4j
public class HttpStatusType implements UserType
{
	@Override
	public int[] sqlTypes() {
		return new int[] { Types.INTEGER };
	}

	@Override
	public Class returnedClass() {
		return HttpStatus.class;
	}

	@Override
	public boolean equals( Object x, Object y ) {
		return Objects.equals( x, y );
	}

	@Override
	public int hashCode( Object x ) {
		return x.hashCode();
	}

	@Override
	public Object nullSafeGet( ResultSet rs, String[] names, SessionImplementor session, Object owner ) throws SQLException {
		int statusCode = rs.getInt( names[0] );
		try {
			return HttpStatus.valueOf( statusCode );
		}
		catch ( IllegalArgumentException iae ) {
			LOG.warn( "{} as persisted in database can not be converted to HttpStatus", statusCode );
			return null;
		}
	}

	@Override
	public void nullSafeSet( PreparedStatement st, Object value, int index, SessionImplementor session ) throws SQLException {
		if ( value == null || !( value instanceof HttpStatus ) ) {
			st.setNull( index, Types.INTEGER );
		}
		else {
			st.setInt( index, ( (HttpStatus) value ).value() );
		}
	}

	@Override
	public Object deepCopy( Object value ) {
		return value;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Serializable disassemble( Object value ) {
		return (Serializable) value;
	}

	@Override
	public Object assemble( Serializable cached, Object owner ) {
		return cached;
	}

	@Override
	public Object replace( Object original, Object target, Object owner ) {
		return original;
	}
}
