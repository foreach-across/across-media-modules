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

package com.foreach.across.modules.webcms.domain.asset.web;

import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.web.WebCmsEndpointContextResolver;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.ConfigurableWebCmsEndpointContext;
import com.foreach.across.modules.webcms.domain.endpoint.web.controllers.InvalidWebCmsEndpointConditionCombination;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Raf Ceuls
 * @since 0.0.2
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsAssetCondition
{
	@Mock
	private ConfigurableWebCmsEndpointContext context;

	@Mock
	private WebCmsEndpointContextResolver resolver;

	private WebCmsAssetEndpoint<WebCmsAsset> endpoint;
	private WebCmsUrl url;

	@Before
	public void setUp() throws Exception {
		endpoint = WebCmsAssetEndpoint.builder().build();
		url = WebCmsUrl.builder().httpStatus( HttpStatus.I_AM_A_TEAPOT ).build();

		when( context.getEndpoint() ).thenReturn( endpoint );
		when( context.isResolved() ).thenReturn( true );
		when( context.getUrl() ).thenReturn( url );
	}

	@Test
	public void emptyConditionContent() throws Exception {
		WebCmsAssetCondition condition = new WebCmsAssetCondition( context, resolver );
		assertEquals( 2, condition.getContent().size() );
	}

	@Test
	public void statusHasPreferenceOverSeries() throws Exception {
		WebCmsAssetCondition conditionWithObjectId = condition( HttpStatus.OK, new String[] { "xyz" } );
		WebCmsAssetCondition conditionWithoutObjectId = condition( HttpStatus.OK );

		int shouldGoFirst = conditionWithObjectId.compareTo( conditionWithoutObjectId, new MockHttpServletRequest() );
		int shouldGoLast = conditionWithoutObjectId.compareTo( conditionWithObjectId, new MockHttpServletRequest() );

		assertEquals( 1, shouldGoLast );
		assertEquals( -1, shouldGoFirst );
	}

	@Test
	public void checkObjectIdControllerIsAllowedToSupersedeMethod() throws Exception {
		WebCmsAssetCondition method = condition( HttpStatus.OK, new String[] { "xyz", "abc" } );
		WebCmsAssetCondition controller = condition( HttpStatus.OK, new String[] { "xyz", "abc", "googly" } );

		WebCmsAssetCondition combined = controller.combine( method );

		assertTrue( Arrays.equals( combined.objectId, new String[] { "xyz", "abc" } ) ); // should do a regular pass
	}

	@Test(expected = InvalidWebCmsEndpointConditionCombination.class)
	public void checkObjectIdControllerIsNotAllowedToBeSupersededByMethod() throws Exception {
		WebCmsAssetCondition controller = condition( HttpStatus.OK, new String[] { "xyz", "abc" } );
		WebCmsAssetCondition method = condition( HttpStatus.OK, new String[] { "xyz", "abc", "googly" } );

		controller.combine( method );
	}

	@Test
	public void checkObjectIdMethodCanBeEmpty() throws Exception {
		WebCmsAssetCondition method = condition( HttpStatus.OK );
		WebCmsAssetCondition controller = condition( HttpStatus.OK, new String[] { "xyz", "abc", "googly" } );

		WebCmsAssetCondition combined = controller.combine( method );

		assertTrue( Arrays.equals( combined.objectId, new String[] { "xyz", "abc", "googly" } ) );
	}

	@Test
	public void checkObjectIdSubsetControllerCanBeEmpty() throws Exception {
		WebCmsAssetCondition method = condition( HttpStatus.OK, new String[] { "xyz", "abc", "googly" } );
		WebCmsAssetCondition controller = condition( HttpStatus.OK );

		controller.combine( method );

		WebCmsAssetCondition combined = controller.combine( method );

		assertTrue( Arrays.equals( combined.objectId, new String[] { "xyz", "abc", "googly" } ) );
	}

	@Test
	public void checkObjectIdBothEmpty() throws Exception {
		WebCmsAssetCondition method = condition( HttpStatus.OK );
		WebCmsAssetCondition controller = condition( HttpStatus.OK );

		controller.combine( method );

		WebCmsAssetCondition combined = controller.combine( method );

		assertTrue( Arrays.equals( combined.objectId, new String[0] ) );
	}

	@Test
	public void conditionWithObjectIdTakesPrecedenceOverNonObjectIdRegardlessOfAssetType() {
		WebCmsAssetCondition one = condition( WebCmsArticle.class, new HttpStatus[0], new HttpStatus.Series[0], new String[0] );
		WebCmsAssetCondition two = condition( WebCmsAsset.class, new HttpStatus[0], new HttpStatus.Series[0], new String[] { "xyz" } );
		WebCmsAssetCondition three = condition( WebCmsAsset.class, new HttpStatus[0], new HttpStatus.Series[0], new String[] { "xyz", "abc" } );

		assertEquals( 1, one.compareTo( two, null ) );
		assertEquals( 1, one.compareTo( three, null ) );
		assertEquals( -1, two.compareTo( three, null ) );
		assertEquals( 1, three.compareTo( two, null ) );
	}

	private WebCmsAssetCondition condition( HttpStatus... statuses ) {
		return condition( WebCmsAsset.class, statuses, new HttpStatus.Series[0], new String[0] );
	}

	private WebCmsAssetCondition condition( Class<? extends WebCmsAsset> endpointType, HttpStatus[] statuses, HttpStatus.Series[] series, String[] objectId ) {
		WebCmsAssetCondition condition = new WebCmsAssetCondition( context, resolver );

		Map<String, Object> values = new HashMap<>();
		values.put( "value", endpointType );
		values.put( "status", statuses );
		values.put( "series", series );
		values.put( "objectId", objectId );

		AnnotatedElement annotatedElement = mock( AnnotatedElement.class );
		when( annotatedElement.getAnnotation( WebCmsAssetMapping.class ) )
				.thenReturn( AnnotationUtils.synthesizeAnnotation( values, WebCmsAssetMapping.class, null ) );
		condition.setAnnotatedElement( annotatedElement );

		return condition;
	}

	private WebCmsAssetCondition condition( HttpStatus status, String[] objectId ) {
		return condition( WebCmsAsset.class, new HttpStatus[] { status }, new HttpStatus.Series[0], objectId );
	}

	class DummyWebCmsPageEndpoint extends WebCmsEndpoint
	{

	}
}