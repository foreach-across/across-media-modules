package com.foreach.imageserver.example.utils;

import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;


public abstract class AbstractContextUtils
{
	public static final String ATTRIBUTE_WEBAPP_CONTEXT = "webappContext";
	public static final String ATTRIBUTE_WEBPATHS = "path";

	public static WebApplicationContext getWebApplicationContext( HttpServletRequest request )
	{
		return (WebApplicationContext) request.getAttribute( ATTRIBUTE_WEBAPP_CONTEXT );
	}

	public static void storeWebApplicationContext( WebApplicationContext ctx, HttpServletRequest request )
	{
		request.setAttribute( ATTRIBUTE_WEBAPP_CONTEXT, ctx );
	}

	public static WebPathConfiguration getWebPathConfiguration( HttpServletRequest request )
	{
		return (WebPathConfiguration) request.getAttribute( ATTRIBUTE_WEBPATHS );
	}

	public static void storeWebPathConfiguration( WebPathConfiguration config, HttpServletRequest request )
	{
		request.setAttribute( ATTRIBUTE_WEBPATHS, config );
	}

	protected AbstractContextUtils()
	{

	}
}

