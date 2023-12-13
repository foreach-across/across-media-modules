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

package com.foreach.across.modules.webcms.infrastructure;

import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Exception to be thrown in case validation fails.
 * The content of {@link Errors} will be appended to the exception message.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
public final class ValidationFailedException extends RuntimeException
{
	private final String errorsList;

	public ValidationFailedException( String message, Errors errors ) {
		this( null, message, errors );
	}

	public ValidationFailedException( MessageSource messageSource, String message, Errors errors ) {
		super( message );

		errorsList =
				"[" + errors.getErrorCount() + "] validation errors: " + System.lineSeparator() + "   "
						+ errors.getAllErrors()
						        .stream()
						        .map( e -> errorToString( e, messageSource ) )
						        .collect( Collectors.joining( System.lineSeparator() + "   " ) );
	}

	@Override
	public String getMessage() {
		return super.getMessage() + ", " + errorsList;
	}

	private String errorToString( ObjectError error, MessageSource messageSource ) {
		if ( error instanceof FieldError ) {
			FieldError fe = (FieldError) error;

			return "Field error in object '" + fe.getObjectName() + "' on field '" + fe.getField() +
					"': rejected value [" + fe.getRejectedValue() + "]; "
					+ ( messageSource != null ? messageSource.getMessage( fe, Locale.US ) : fe.getDefaultMessage() );
		}

		return "Error in object '" + error.getObjectName() + "': "
				+ ( messageSource != null ? messageSource.getMessage( error, Locale.US ) : error.getDefaultMessage() );
	}
}
