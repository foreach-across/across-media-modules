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

package com.foreach.across.modules.webcms.domain.domain.web;

import com.foreach.across.modules.web.mvc.condition.AbstractCustomRequestCondition;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import com.foreach.across.modules.webcms.domain.endpoint.web.controllers.WebCmsConditionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Collections;

import static com.foreach.across.modules.webcms.domain.endpoint.web.controllers.WebCmsConditionUtils.compareArrays;

/**
 * Condition for {@link WebCmsDomainMapping}.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
@RequiredArgsConstructor
class WebCmsDomainCondition extends AbstractCustomRequestCondition<WebCmsDomainCondition>
{
	private final WebCmsMultiDomainService multiDomainService;

	private String[] domains = {};

	@Override
	public void setAnnotatedElement( AnnotatedElement annotatedElement ) {
		WebCmsDomainMapping domainMapping = AnnotatedElementUtils.findMergedAnnotation( annotatedElement, WebCmsDomainMapping.class );
		domains = domainMapping.value();
	}

	@Override
	protected Collection<?> getContent() {
		return Collections.singletonList( domains );
	}

	@Override
	protected String getToStringInfix() {
		return "";
	}

	@Override
	public WebCmsDomainCondition combine( WebCmsDomainCondition other ) {
		WebCmsDomainCondition result = new WebCmsDomainCondition( multiDomainService );
		result.domains = WebCmsConditionUtils.combineArrays( this.domains, other.domains );
		return result;
	}

	@Override
	public WebCmsDomainCondition getMatchingCondition( HttpServletRequest request ) {
		if ( domains.length == 0 ) {
			return this;
		}

		WebCmsDomain domain = multiDomainService.getCurrentDomain();

		for ( String candidate : domains ) {
			if ( candidate == null && domain == null ) {
				WebCmsDomainCondition condition = new WebCmsDomainCondition( multiDomainService );
				condition.domains = new String[] { null };
				return condition;
			}
			else if ( domain != null && domain.getDomainKey().equals( candidate ) ) {
				WebCmsDomainCondition condition = new WebCmsDomainCondition( multiDomainService );
				condition.domains = new String[] { candidate };
				return condition;
			}
		}

		return null;
	}

	@Override
	public int compareTo( WebCmsDomainCondition other, HttpServletRequest request ) {
		return compareArrays( domains, other.domains );
	}
}
