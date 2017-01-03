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

package com.foreach.across.modules.webcms.web;

import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.WebCmsPageRepository;
import com.foreach.across.modules.webcms.web.page.template.PageTemplateResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Controller
@Slf4j
public class WebCmsEndpointController
{
	private final UrlPathHelper pathHelper = new UrlPathHelper();

	private WebCmsPageRepository pageRepository;
	private PageTemplateResolver pageTemplateResolver;

	@RequestMapping
	public ModelAndView renderEndpoint( HttpServletRequest request, HttpServletResponse response, ModelAndView mav ) {
		String path = pathHelper.getPathWithinApplication( request );
		WebCmsPage page = pageRepository.findByPath( path );

		if ( page == null ) {
			LOG.trace( "No WebCmsEndpoint found for: {}", path );
			throw new NoSuchEndpointException( path );
		}

		mav.addObject( "page", page );
		mav.setViewName( pageTemplateResolver.resolvePageTemplate( page.getTemplate() ) );

		return mav;
	}

	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	private static class NoSuchEndpointException extends RuntimeException
	{
		NoSuchEndpointException( String path ) {
			super( "No @RequestMapping or WebCmsEndpoint found for: " + path );
		}
	}

	@Autowired
	void setPageRepository( WebCmsPageRepository pageRepository ) {
		this.pageRepository = pageRepository;
	}

	@Autowired
	void setPageTemplateResolver( PageTemplateResolver pageTemplateResolver ) {
		this.pageTemplateResolver = pageTemplateResolver;
	}
}
