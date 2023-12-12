package com.foreach.imageserver.core.controllers;

import com.foreach.imageserver.dto.JsonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

public abstract class BaseImageAPIController
{
	private static final Logger LOG = LoggerFactory.getLogger( BaseImageAPIController.class );

	protected String accessToken;

	protected BaseImageAPIController( String accessToken ) {
		this.accessToken = accessToken;
	}

	public void setAccessToken( String accessToken ) {
		this.accessToken = accessToken;
	}

	protected JsonResponse<Void> error( String message ) {
		JsonResponse<Void> result = new JsonResponse<>();
		result.setSuccess( false );
		result.setErrorMessage( message );
		return result;
	}

	protected <T> JsonResponse<T> success( T result ) {
		JsonResponse<T> jsonResponse = success();
		jsonResponse.setResult( result );
		return jsonResponse;
	}

	protected <T> JsonResponse<T> success() {
		JsonResponse<T> jsonResponse = new JsonResponse<>();
		jsonResponse.setSuccess( true );
		return jsonResponse;
	}

	/**
	 * Make sure that for controller methods that fail with an exception, we still return some pretty json
	 */

	@ExceptionHandler(Exception.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public JsonResponse handleError( HttpServletRequest req, Exception exception ) {
		LOG.error( exception.getMessage()  );
		return error( exception.getMessage() );
	}
}
