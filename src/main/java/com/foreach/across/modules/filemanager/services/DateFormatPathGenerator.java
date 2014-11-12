package com.foreach.across.modules.filemanager.services;

import org.apache.commons.lang3.time.FastDateFormat;

/**
 * Generates a path based on the current timestamp and a date format pattern.
 */
public class DateFormatPathGenerator implements PathGenerator
{
	public static final DateFormatPathGenerator YEAR_MONTH = new DateFormatPathGenerator( "yyyy/MM" );
	public static final DateFormatPathGenerator YEAR_MONTH_DAY = new DateFormatPathGenerator( "yyyy/MM/dd" );

	private final FastDateFormat dateFormat;

	public DateFormatPathGenerator( String pattern ) {
		this.dateFormat = FastDateFormat.getInstance( pattern );
	}

	@Override
	public String generatePath() {
		return dateFormat.format( System.currentTimeMillis() );
	}
}
