package com.foreach.imageserver.core.business;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.BigDecimalType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class ImageTypeUserType implements UserType
{
	public static final String CLASS_NAME = "com.foreach.imageserver.core.business.ImageTypeUserType";
	private static final BigDecimalType TYPE = BigDecimalType.INSTANCE;

	@Override
	public int[] sqlTypes() {
		return new int[] { TYPE.sqlType() };
	}

	@Override
	public Class returnedClass() {
		return ImageType.class;
	}

	@Override
	public boolean equals( Object x, Object y ) throws HibernateException {
		return Objects.equals( x, y );
	}

	@Override
	public int hashCode( Object x ) throws HibernateException {
		return Objects.hashCode( x );
	}

	@Override
	public Object nullSafeGet( ResultSet rs,
	                           String[] names,
	                           SessionImplementor session,
	                           Object owner ) throws HibernateException, SQLException {
		BigDecimal id = (BigDecimal) TYPE.get( rs, names[0], session );
		for ( ImageType imageType : ImageType.values() ) {
			if ( imageType.getId().equals( id ) ) {
				return imageType;
			}
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void nullSafeSet( PreparedStatement st,
	                         Object value,
	                         int index,
	                         SessionImplementor session ) throws HibernateException, SQLException {
		try {
			BigDecimal result = ( (ImageType) value ).getId();
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
