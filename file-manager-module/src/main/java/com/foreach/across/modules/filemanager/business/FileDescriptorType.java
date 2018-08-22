package com.foreach.across.modules.filemanager.business;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
public class FileDescriptorType implements UserType
{
	@Override
	public int[] sqlTypes() {
		return new int[] { Types.VARCHAR };
	}

	@Override
	public Class returnedClass() {
		return FileDescriptor.class;
	}

	@Override
	public boolean equals( Object x, Object y ) throws HibernateException {
		return Objects.equals( x, y );
	}

	@Override
	public int hashCode( Object x ) throws HibernateException {
		return x.hashCode();
	}

	@Override
	public Object nullSafeGet( ResultSet rs, String[] names, SessionImplementor session, Object owner ) throws HibernateException, SQLException {
		String value = rs.getString( names[0] );
		return FileDescriptor.of( value );
	}

	@Override
	public void nullSafeSet( PreparedStatement st, Object value, int index, SessionImplementor session ) throws HibernateException, SQLException {
		if ( value == null ) {
			st.setNull( index, Types.VARCHAR );
		}
		else {
			st.setString( index, ( (FileDescriptor) value ).getUri() );
		}
	}

	@Override
	public Object deepCopy( Object value ) throws HibernateException {
		return value;
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public Serializable disassemble( Object value ) throws HibernateException {
		return (Serializable) value;
	}

	@Override
	public Object assemble( Serializable cached, Object owner ) throws HibernateException {
		return cached;
	}

	@Override
	public Object replace( Object original, Object target, Object owner ) throws HibernateException {
		return target;
	}
}
