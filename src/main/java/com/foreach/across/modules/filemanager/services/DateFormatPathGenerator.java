/*
 * Copyright 2014 the original author or authors
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
