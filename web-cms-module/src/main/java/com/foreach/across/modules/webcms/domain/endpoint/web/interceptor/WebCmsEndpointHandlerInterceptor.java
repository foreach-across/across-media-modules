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

package com.foreach.across.modules.webcms.domain.endpoint.web.interceptor;

import com.foreach.across.modules.bootstrapui.resource.JQueryWebResources;
import com.foreach.across.modules.web.events.BuildTemplateWebResourcesEvent;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRule;
import com.foreach.across.modules.webcms.WebCmsModule;
import com.foreach.across.modules.webcms.domain.endpoint.web.IgnoreEndpointModel;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.WebCmsEndpointContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.foreach.across.modules.webcms.domain.endpoint.web.WebCmsEndpointControllerAdvice.LOAD_ENDPOINT_MODEL_ATTRIBUTE;

/**
 * Interceptor for basic {@link com.foreach.across.modules.webcms.domain.url.WebCmsUrl} and
 * {@link com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint} handling.
 * <p/>
 * <ul>
 * <li>sets the status code of the {@link com.foreach.across.modules.webcms.domain.url.WebCmsUrl} on the {@link HttpServletResponse}</li>
 * <li>sets the <strong>X-WCM-Preview</strong> header when previewing an endpoint</li>
 * <li>will apply the default template as view if one is set as request attribute ({@link #DEFAULT_TEMPLATE_ATTRIBUTE} and the handler method
 * has a void return type</li>
 * </ul>
 * <p/>
 * This interceptor also listens to the {@link BuildTemplateWebResourcesEvent} and will register preview related web resources when in preview mode.
 *
 * @author Sander Van Loock, Arne Vandamme
 * @see com.foreach.across.modules.webcms.domain.endpoint.web.WebCmsEndpointControllerAdvice
 * @see com.foreach.across.modules.webcms.domain.endpoint.web.IgnoreEndpointModel
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
public class WebCmsEndpointHandlerInterceptor extends HandlerInterceptorAdapter
{
	/**
	 * Attribute value should be the view that will be resolved if a handler method has a void return type.
	 */
	public static final String DEFAULT_TEMPLATE_ATTRIBUTE = WebCmsEndpointHandlerInterceptor.class.getName() + ".DEFAULT_TEMPLATE";

	private final WebCmsEndpointContext context;

	@Override
	public boolean preHandle( HttpServletRequest request, HttpServletResponse response, Object handler ) throws Exception {
		if ( context.isAvailable() ) {
			response.setStatus( context.getUrl().getHttpStatus().value() );

			if ( context.isPreviewMode() ) {
				response.setHeader( "X-WCM-Preview", "true" );
			}

			if ( !shouldEndpointModelBeIgnored( handler ) ) {
				request.setAttribute( LOAD_ENDPOINT_MODEL_ATTRIBUTE, true );
			}
		}

		return true;
	}

	private boolean shouldEndpointModelBeIgnored( Object handler ) {
		if ( handler instanceof HandlerMethod ) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			IgnoreEndpointModel ignoreAnnotation = handlerMethod.getMethodAnnotation( IgnoreEndpointModel.class );

			if ( ignoreAnnotation == null ) {
				Class<?> controllerClass = handlerMethod.getBeanType();
				ignoreAnnotation = AnnotationUtils.findAnnotation( controllerClass, IgnoreEndpointModel.class );
			}

			return ignoreAnnotation != null;
		}

		return true;
	}

	@Override
	public void postHandle( HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView ) throws Exception {
		if ( handler instanceof HandlerMethod ) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			String defaultTemplate = (String) request.getAttribute( DEFAULT_TEMPLATE_ATTRIBUTE );

			if ( handlerMethod.getReturnType().getMethod().getReturnType().equals( void.class ) && defaultTemplate != null ) {
				LOG.trace( "Applying default template {} as viewName since handler method had a void return type", defaultTemplate );
				modelAndView.setViewName( defaultTemplate );
			}
		}
	}

	@EventListener
	public void registerPreviewModeWebResources( BuildTemplateWebResourcesEvent webResourcesEvent ) {
		if ( context.isAvailable() && context.isPreviewMode() ) {
			webResourcesEvent.applyResourceRules(
					WebResourceRule.addPackage( JQueryWebResources.NAME ),
					WebResourceRule.add( WebResource.css( "@static:/WebCmsModule/css/wcm-inline-editor.css" ) )
					               .withKey( WebCmsModule.NAME + "-inline-editor" )
					               .toBucket( WebResource.CSS ),
					WebResourceRule.add( WebResource.javascript( "@static:/WebCmsModule/js/wcm-preview-mode.js" ) )
					               .withKey( WebCmsModule.NAME + "-preview" )
					               .toBucket( WebResource.JAVASCRIPT_PAGE_END )
			);
		}
	}
}
