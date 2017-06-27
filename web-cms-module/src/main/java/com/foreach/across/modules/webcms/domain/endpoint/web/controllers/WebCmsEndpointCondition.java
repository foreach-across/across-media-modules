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

package com.foreach.across.modules.webcms.domain.endpoint.web.controllers;

import com.foreach.across.modules.web.mvc.condition.AbstractCustomRequestCondition;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.web.WebCmsEndpointContextResolver;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.ConfigurableWebCmsEndpointContext;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.WebCmsEndpointContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A condition to use for retrieving the correct {@link WebCmsEndpoint}.  This condition takes all properties of
 * {@link WebCmsEndpointMapping} into account.
 *
 * @author Sander Van Loock
 * @since 0.0.1
 * @see com.foreach.across.modules.web.mvc.condition.CustomRequestCondition
 */
@RequiredArgsConstructor
@Slf4j
public class WebCmsEndpointCondition extends AbstractCustomRequestCondition<WebCmsEndpointCondition>
{
	private final ConfigurableWebCmsEndpointContext context;
	private final WebCmsEndpointContextResolver resolver;

	private Class<? extends WebCmsEndpoint> endpointType = WebCmsEndpoint.class;
	private HttpStatus[] statuses = {};
	private HttpStatus.Series[] series = {};

	/**
	 * Set the values for this condition based on the attributes of the annotated element.
	 *
	 * @param annotatedElement this condition is attached to
	 */
	@Override
	public void setAnnotatedElement( AnnotatedElement annotatedElement ) {
		WebCmsEndpointMapping endpointMapping = AnnotatedElementUtils.findMergedAnnotation( annotatedElement, WebCmsEndpointMapping.class );

		endpointType = endpointMapping.value();
		statuses = endpointMapping.status();
		series = endpointMapping.series();
	}

	@Override
	protected Collection<?> getContent() {
		return Arrays.asList( endpointType, statuses, series );
	}

	@Override
	protected String getToStringInfix() {
		return " && ";
	}

	/**
	 * <p>
	 * Two conditions are merged by modifying this instance of {@link WebCmsEndpointCondition}.
	 * The classes of the conditions are combined to the most specific child in the inheritance tree.  If the two
	 * {@link WebCmsEndpoint} types are unrelated, a {@link InvalidWebCmsEndpointConditionCombination} is thrown.
	 * <p>
	 * Both the {@link HttpStatus}es and {@link HttpStatus.Series} are combined.
	 * To combine these arrays we take the intersection of both arrays if both are not empty.  If only one array is not empty, we take
	 * this one.  If both are empty, keep them empty.
	 * <p>
	 * The resulting array of {@link HttpStatus}es will be appended with statuses that are in one's status and in other ones series.
	 * </p>
	 * {@code
	 *
	 * @WebCmsEndpointMapping(status={HttpStatus.502,HttpStatus.501},series={HttpStatus.Series.SUCCESSFUL,HttpStatus.Series.REDIRECTION,HttpStatus.Series.CLIENT_ERROR}) public class MyController{
	 * @WebCmsEndpointMapping(status={HttpStatus.501,HttpStatus.301},serires={HttpStatus.Series.CLIENT_ERROR) public String myMethod(){
	 * ...
	 * }
	 * }
	 * }
	 * <p>
	 * must be combined into a{@link WebCmsEndpointCondition} with status 501 and series CLIENT_ERROR because the both share this status and series.
	 * The status 301 is also added because 301 the first contains the REDIRECTION serie and the other the 301 status.
	 */
	@Override
	public WebCmsEndpointCondition combine( WebCmsEndpointCondition other ) {
		WebCmsEndpointCondition result = new WebCmsEndpointCondition( this.context, this.resolver );
		if ( this.endpointType.isAssignableFrom( other.endpointType ) ) {
			result.endpointType = other.endpointType;
		}
		else if ( !other.endpointType.isAssignableFrom( this.endpointType ) ) {
			String message = String.format( "A condition with endpoint type %s and type %s cannot be merged", this.endpointType,
			                                other.endpointType );
			throw new InvalidWebCmsEndpointConditionCombination( message );
		}
		else {
			result.endpointType = this.endpointType;
		}
		result.statuses = intersect( this.statuses, other.statuses, this.series, other.series );
		result.series = intersect( this.series, other.series );
		return result;
	}

	/**
	 * A condition can only match if the resolved {@link WebCmsEndpointContext} has an endpoint of the correct class and
	 * a url with correct statuscode.
	 * <p>
	 * Only the matching {@link HttpStatus} or {@link HttpStatus.Series} from the {@link WebCmsEndpointContext} is put on the resulting condition
	 *
	 * @param request The {@link HttpServletRequest} which is used for resolving the {@link WebCmsEndpointContext}
	 * @return A new instance with only the {@link HttpStatus} from the {@link WebCmsEndpointContext}
	 */
	@Override
	public WebCmsEndpointCondition getMatchingCondition( HttpServletRequest request ) {
		if ( !context.isResolved() ) {
			resolver.resolve( context, request );
		}
		if ( endpointType.isInstance( context.getEndpoint() ) && hasCorrectStatus( context.getUrl().getHttpStatus() ) ) {
			WebCmsEndpointCondition result = new WebCmsEndpointCondition( context, resolver );
			result.endpointType = this.endpointType;
			if ( this.series.length == 0 ) {
				result.statuses = new HttpStatus[] { context.getUrl().getHttpStatus() };
			}
			else {
				result.series = new HttpStatus.Series[] { context.getUrl().getHttpStatus().series() };
			}
			LOG.trace( "Matching condition is {}", result );
			return result;
		}
		return null;
	}

	/**
	 * Returns -1, 0 or 1 if the current {@link WebCmsEndpointCondition} is more specific, equally specific or less specific
	 * than the given condition.  How specific a {@link WebCmsEndpointCondition} is, is first determined by the class of
	 * the {@link WebCmsEndpoint}.  Secondly, the number of {@link HttpStatus}es is compared and lastly, the number of
	 * {@link HttpStatus.Series} is checked.
	 *
	 * @param other   The {@link WebCmsEndpointCondition} to compare with
	 * @param request The current {@link HttpServletRequest}.  This is not considered for comparing
	 * @return -1, 0 or 1 if the current {@link WebCmsEndpointCondition} is more specific, equally specific or less specific
	 * than the given condition
	 */
	@Override
	public int compareTo( WebCmsEndpointCondition other, HttpServletRequest request ) {
		if ( endpointType != null && other.endpointType != null && !endpointType.equals( other.endpointType ) ) {
			return endpointType.isAssignableFrom( other.endpointType ) ? 1 : -1;
		}
		if ( statuses != null && other.statuses != null && statuses.length != other.statuses.length ) {
			return statuses.length > other.statuses.length ? 1 : -1;
		}
		if ( series != null && other.series != null && series.length != other.series.length ) {
			return series.length > other.series.length ? 1 : -1;
		}
		return 0;
	}

	private HttpStatus[] intersect( HttpStatus[] first, HttpStatus[] other, HttpStatus.Series[] firstSeries, HttpStatus.Series[] otherSeries ) {
		if ( first.length == 0 || other.length == 0 ) {
			return first.length == 0 ? other : first;
		}
		Set<HttpStatus> result = new HashSet<>( Arrays.asList( first ) );
		result.retainAll( new HashSet<>( Arrays.asList( other ) ) );

		addStatusIfIntersectSeries( firstSeries, first, other, result );
		addStatusIfIntersectSeries( otherSeries, first, other, result );

		return result.toArray( new HttpStatus[result.size()] );
	}

	private void addStatusIfIntersectSeries( HttpStatus.Series[] firstSeries, HttpStatus[] first, HttpStatus[] other, Set<HttpStatus> result ) {
		for ( HttpStatus.Series serie : firstSeries ) {
			for ( HttpStatus status : first ) {
				addStatusIfInSeries( result, serie, status );
			}
			for ( HttpStatus status : other ) {
				addStatusIfInSeries( result, serie, status );
			}
		}
	}

	private void addStatusIfInSeries( Set<HttpStatus> s1, HttpStatus.Series serie, HttpStatus status ) {
		if ( status.series().equals( serie ) ) {
			s1.add( status );
		}
	}

	private HttpStatus.Series[] intersect( HttpStatus.Series[] first, HttpStatus.Series[] other ) {
		if ( first.length == 0 || other.length == 0 ) {
			return first.length == 0 ? other : first;
		}
		Set<HttpStatus.Series> result = new HashSet<>( Arrays.asList( first ) );
		result.retainAll( new HashSet<>( Arrays.asList( other ) ) );

		return result.toArray( new HttpStatus.Series[result.size()] );
	}

	private boolean hasCorrectStatus( HttpStatus currentStatus ) {
		HttpStatus.Series currentSeries = currentStatus.series();
		boolean seriesMatch = series.length == 0 || Stream.of( series ).anyMatch( currentSeries::equals );
		boolean statusMatch = statuses.length == 0 || Stream.of( statuses ).anyMatch( currentStatus::equals );
		return seriesMatch && statusMatch;
	}
}
