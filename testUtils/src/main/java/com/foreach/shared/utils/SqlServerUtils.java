package com.foreach.shared.utils;

import java.util.Date;

public final class SqlServerUtils
{
	private SqlServerUtils()
	{
	}

	public static boolean datesCloseEnough( Date d1, Date d2, long deltaInSeconds )
	{
		long t1 = d1.getTime();
		long t2 = d2.getTime();

		return ( Math.abs( t2-t1 ) < deltaInSeconds );
	}

	public static boolean equalSmallDateTimes( Date d1, Date d2)
	{
		if ( ( d1==null ) || (d2 == null ) ) {
			return ( ( d1 == null ) && ( d2 == null ) );
		}

		return ( datesCloseEnough( d1, d2, 60 * 1000 ) );
	}

	// SQL Server is only accurate to 3/1000ths of a second.

	public static boolean equalDateTimes( Date d1, Date d2)
	{
		if ( ( d1==null ) || (d2 == null ) ) {
			return ( ( d1 == null ) && ( d2 == null ) );
		}

		return ( datesCloseEnough( d1, d2, 3 ) );
	}
}
