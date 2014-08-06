package com.foreach.imageserver.core.business;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StringType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TagsUserType implements UserType
{
	public static final String CLASS_NAME = "com.foreach.imageserver.core.business.TagsUserType";
	private final StringType TYPE = StringType.INSTANCE;

	public int[] sqlTypes() {
		return new int[] { TYPE.sqlType() };
	}

	@Override
	public Class returnedClass() {
		return Set.class;
	}

	@Override
	public boolean equals( Object x, Object y ) throws HibernateException {
		return x == y;
	}

	@Override
	public int hashCode( Object x ) throws HibernateException {
		return x.hashCode();
	}

	@Override
	public Object nullSafeGet( ResultSet rs,
	                           String[] names,
	                           SessionImplementor session,
	                           Object owner ) throws HibernateException, SQLException {
		String value = (String) TYPE.get( rs, names[0], session );
		return new HashSet<>( Arrays.asList( StringUtils.split( StringUtils.defaultString( value ), "," ) ) );
	}

	@Override
	@SuppressWarnings("unchecked")
	public void nullSafeSet( PreparedStatement st,
	                         Object value,
	                         int index,
	                         SessionImplementor session ) throws HibernateException, SQLException {
		try {
			Set<String> tags = new HashSet<>();

			for ( String p : (Set<String>) value ) {
				if ( StringUtils.isNotBlank( p ) ) {
					tags.add( p );
				}
			}
			String result = StringUtils.join( tags, "," );
			TYPE.set( st, result, index, session );
		}
		catch ( Exception e ) {
			throw new HibernateException( "Exception while getting ids from set", e );
		}
	}

	@Override
	public Object deepCopy( Object value ) throws HibernateException {
		return value;
	}

	@Override
	public boolean isMutable() {
		return false;
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
		return original;
	}
}