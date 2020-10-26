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

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import com.foreach.across.modules.webcms.domain.endpoint.web.controllers.InvalidWebCmsConditionCombination;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.annotation.AnnotationUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
@ExtendWith(MockitoExtension.class)
public class TestWebCmsDomainCondition
{
	@Mock
	private WebCmsMultiDomainService multiDomainService;

	@Mock
	private HttpServletRequest request;

	@Test
	public void emptyConditionContent() throws Exception {
		WebCmsDomainCondition condition = new WebCmsDomainCondition( multiDomainService );

		Collection<?> actualContent = condition.getContent();
		assertEquals( 1, actualContent.size() );
	}

	@Test
	public void stringInfix() throws Exception {
		WebCmsDomainCondition condition = new WebCmsDomainCondition( multiDomainService );
		assertEquals( "", condition.getToStringInfix() );
	}

	@Test
	public void conditionWithDomainsAlwaysTakePrecedenceOverNoDomainSpecified() throws Exception {
		WebCmsDomainCondition noDomains = condition();
		WebCmsDomainCondition singleDomain = condition( "one" );
		WebCmsDomainCondition multipleDomains = condition( "two", null );

		assertEquals( 0, noDomains.compareTo( noDomains, request ) );
		assertEquals( 0, singleDomain.compareTo( singleDomain, request ) );
		assertEquals( 0, multipleDomains.compareTo( multipleDomains, request ) );
		assertEquals( 1, noDomains.compareTo( singleDomain, request ) );
		assertEquals( -1, singleDomain.compareTo( multipleDomains, request ) );
		assertEquals( 1, noDomains.compareTo( multipleDomains, request ) );
		assertEquals( -1, singleDomain.compareTo( noDomains, request ) );
		assertEquals( 1, multipleDomains.compareTo( singleDomain, request ) );
		assertEquals( -1, multipleDomains.compareTo( noDomains, request ) );
	}

	@Test
	public void dontMatchIfWrongDomain() throws Exception {
		WebCmsDomainCondition condition = condition( "specific-domain" );

		WebCmsDomainCondition actualCondition = condition.getMatchingCondition( request );
		assertNull( actualCondition );
	}

	@Test
	public void matchIfRightDomain() throws Exception {
		WebCmsDomainCondition condition = condition( "some-domain", WebCmsDomain.NO_DOMAIN_KEY );

		WebCmsDomainCondition actualCondition = condition.getMatchingCondition( request );
		verifyCondition( actualCondition, new String[] { null } );

		when( multiDomainService.getCurrentDomain() ).thenReturn( WebCmsDomain.builder().domainKey( "some-domain" ).build() );
		actualCondition = condition.getMatchingCondition( request );
		verifyCondition( actualCondition, "some-domain" );
	}

	@Test
	public void combineEmtpyAndFullCondition() throws Exception {
		WebCmsDomainCondition empty = condition();
		WebCmsDomainCondition noDomain = condition( "some-domain" );

		WebCmsDomainCondition actual = empty.combine( noDomain );
		assertNotNull( actual );
		verifyCondition( actual, "some-domain" );
	}

	@Test
	public void combineTwoConditions() throws Exception {
		WebCmsDomainCondition multiple = condition( WebCmsDomain.NO_DOMAIN_KEY, "some-domain" );
		WebCmsDomainCondition single = condition( "some-domain" );

		WebCmsDomainCondition actual = multiple.combine( single );
		assertNotNull( actual );
		verifyCondition( actual, "some-domain" );
	}

	@Test
	public void invalidCombineDueToDifferentDomains() throws Exception {
		WebCmsDomainCondition conditionOne = condition( null, "one" );
		WebCmsDomainCondition conditionTwo = condition( "two", null );

		boolean exceptionThrown = false;
		try {
			conditionOne.combine( conditionTwo );
		}
		catch ( InvalidWebCmsConditionCombination e ) {
			String message = "Unable to combine endpoints: method level must be same or narrower than controller: [two, null] is not a subset of [null, one]";
			assertEquals( message, e.getLocalizedMessage() );
			exceptionThrown = true;
		}
		assertTrue( exceptionThrown );
	}

	private WebCmsDomainCondition condition( String... domains ) {
		WebCmsDomainCondition condition = new WebCmsDomainCondition( multiDomainService );

		Map<String, Object> values = new HashMap<>();
		values.put( "value", domains );

		AnnotatedElement annotatedElement = mock( AnnotatedElement.class );
		when( annotatedElement.getDeclaredAnnotations() )
				.thenReturn( new Annotation[] { AnnotationUtils.synthesizeAnnotation( values, WebCmsDomainMapping.class, null ) } );
		condition.setAnnotatedElement( annotatedElement );

		return condition;
	}

	private void verifyCondition( WebCmsDomainCondition condition, String... domains ) {
		List<?> content = (List<?>) condition.getContent();
		assertArrayEquals( domains, (String[]) content.get( 0 ) );
	}
}
