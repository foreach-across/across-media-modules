package com.foreach.imageserver.dao.selectors;

public abstract class AbstractSelector
{
	protected final int addMultiplyHash( int a, Object b )
	{
		return 31 * a + ( b != null ? b.hashCode() : 0 );
	}

	protected final boolean nullSafeCompare( Object a, Object b )
	{
		return ( a == null ) ? ( b == null ) : a.equals( b );
	}

}

