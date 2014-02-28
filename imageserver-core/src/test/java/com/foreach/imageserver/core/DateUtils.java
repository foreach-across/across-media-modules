package com.foreach.imageserver.core;

import java.text.ParseException;
import java.util.Date;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils
{
	public static Date parseDate( String date ) {
		try {
			return parseDate( date, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd" );
		}
		catch ( ParseException pe ) {
			return null;
		}
	}
}
