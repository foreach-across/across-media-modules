package com.foreach.imageserver.web.interceptors;

import com.foreach.imageserver.web.context.AbstractContextUtils;
import com.foreach.imageserver.web.context.WebPathConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GlobalVariableInterceptor extends HandlerInterceptorAdapter
{
	public static final String ATTRIBUTE_SERVERNAME = "serverName";

	@Autowired
	private WebPathConfiguration webPathConfiguration;

	@Override
	public final boolean preHandle( HttpServletRequest request, HttpServletResponse response, Object handler ) {
		AbstractContextUtils.storeWebPathConfiguration( webPathConfiguration, request );

		request.setAttribute( ATTRIBUTE_SERVERNAME, request.getServerName() );

		setAdditionalVariables( request, response );

		return true;
	}

	protected void setAdditionalVariables( HttpServletRequest request, HttpServletResponse response ) {
	}
}
