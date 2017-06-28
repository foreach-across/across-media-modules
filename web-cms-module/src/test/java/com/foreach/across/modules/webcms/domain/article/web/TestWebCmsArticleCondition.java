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

package com.foreach.across.modules.webcms.domain.article.web;

import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticleType;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.web.WebCmsEndpointContextResolver;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.ConfigurableWebCmsEndpointContext;
import com.foreach.across.modules.webcms.domain.endpoint.web.controllers.InvalidWebCmsEndpointConditionCombination;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublication;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublicationType;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;

import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Raf Ceuls
 * @since 0.0.2
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsArticleCondition
{
	@Mock
	private ConfigurableWebCmsEndpointContext context;

	@Mock
	private WebCmsEndpointContextResolver resolver;

	@Before
	public void setUp() throws Exception {
		WebCmsAssetEndpoint<WebCmsAsset> endpoint = WebCmsAssetEndpoint.builder().build();
		WebCmsUrl url = WebCmsUrl.builder().httpStatus( HttpStatus.I_AM_A_TEAPOT ).build();

		when( context.getEndpoint( WebCmsAssetEndpoint.class ) ).thenReturn( endpoint );
		when( context.isResolved() ).thenReturn( true );
		when( context.getUrl() ).thenReturn( url );
		when( context.isOfType( WebCmsAssetEndpoint.class ) ).thenReturn( true );

		WebCmsArticle article = new WebCmsArticle();
		article.setArticleType( WebCmsArticleType.builder().objectId( "wcm:type:article:oid" ).typeKey( "article-type" ).build() );
		article.setPublication(
				WebCmsPublication.builder()
				                 .publicationKey( "publication" ).objectId( "wcm:asset:publication:oid" )
				                 .publicationType( WebCmsPublicationType.builder().objectId( "wcm:type:publication:oid" ).typeKey( "publication-type" )
				                                                        .build() )
				                 .build()

		);

		endpoint.setAsset( article );
	}

	@Test
	public void emptyConditionContent() throws Exception {
		WebCmsArticleCondition condition = new WebCmsArticleCondition( context, resolver );
		assertEquals( 3, condition.getContent().size() );
	}

	@Test
	public void conditionWithoutAttributesMatchesOnEveryArticle() {
		WebCmsArticleCondition match = condition( new String[0], new String[0], new String[0] ).getMatchingCondition( null );
		assertArrayEquals( new String[0], match.articleTypes );
		assertArrayEquals( new String[0], match.publicationTypes );
		assertArrayEquals( new String[0], match.publications );
	}

	@Test
	public void noMatchIfAttributeNotOk() {
		assertNull( condition( new String[] { "xyz" }, new String[0], new String[0] ).getMatchingCondition( null ) );
		assertNull( condition( new String[0], new String[] { "xyz" }, new String[0] ).getMatchingCondition( null ) );
		assertNull( condition( new String[0], new String[0], new String[] { "xyz" } ).getMatchingCondition( null ) );
	}

	@Test
	public void matchReturnsOriginalAttributeValues() {
		assertMatch( new String[] { "xyz", "article-type" }, new String[0], new String[0] );
		assertMatch( new String[0], new String[] { "xyz", "publication-type" }, new String[0] );
		assertMatch( new String[0], new String[0], new String[] { "xyz", "publication" } );
		assertMatch( new String[] { "xyz", "wcm:type:article:oid" }, new String[] { "xyz", "wcm:type:publication:oid" }, new String[0] );
		assertMatch( new String[] { "xyz", "wcm:type:article:oid" }, new String[0], new String[] { "xyz", "wcm:asset:publication:oid" } );
		assertMatch( new String[] { "xyz", "wcm:type:article:oid" }, new String[] { "xyz", "wcm:type:publication:oid" },
		             new String[] { "xyz", "wcm:asset:publication:oid" } );
	}

	private void assertMatch( String[] articleTypes, String[] publicationTypes, String[] publications ) {
		WebCmsArticleCondition match = condition( articleTypes, publicationTypes, publications ).getMatchingCondition( null );
		assertArrayEquals( articleTypes, match.articleTypes );
		assertArrayEquals( publicationTypes, match.publicationTypes );
		assertArrayEquals( publications, match.publications );
	}

	@Test
	public void combinationIsTheUnionOfAttributesIfOneSideIsEmpty() {
		WebCmsArticleCondition combination = condition( new String[] { "xyz" }, new String[0], new String[0] )
				.combine( condition( new String[0], new String[] { "abc" }, new String[0] ) )
				.combine( condition( new String[0], new String[0], new String[] { "123" } ) );

		assertNotNull( combination );
		assertArrayEquals( new String[] { "xyz" }, combination.articleTypes );
		assertArrayEquals( new String[] { "abc" }, combination.publicationTypes );
		assertArrayEquals( new String[] { "123" }, combination.publications );
	}

	@Test
	public void combinationIsTheJunctionIfParentScopeIsWider() {
		WebCmsArticleCondition combination = condition( new String[] { "xyz", "xyz2" }, new String[] { "abc2", "abc" }, new String[] { "123", "456" } )
				.combine( condition( new String[] { "xyz" }, new String[] { "abc" }, new String[] { "123" } ) );

		assertNotNull( combination );
		assertArrayEquals( new String[] { "xyz" }, combination.articleTypes );
		assertArrayEquals( new String[] { "abc" }, combination.publicationTypes );
		assertArrayEquals( new String[] { "123" }, combination.publications );
	}

	@Test(expected = InvalidWebCmsEndpointConditionCombination.class)
	public void combineFailsIfChildScopeIsWider() {
		condition( new String[] { "xyz" }, new String[] { "abc" }, new String[] { "123" } )
				.combine( condition( new String[] { "xyz", "xyz2" }, new String[] { "abc2", "abc" }, new String[] { "123", "456" } ) );
	}

	@Test
	public void publicationHasPrecedenceOverPublicationTypeAndArticleType() {
		WebCmsArticleCondition left = condition( new String[0], new String[0], new String[] { "xyz" } );
		WebCmsArticleCondition right = condition( new String[0], new String[] { "123" }, new String[0] );

		assertEquals( -1, left.compareTo( right, null ) );
		assertEquals( 1, right.compareTo( left, null ) );

		right = condition( new String[] { "123" }, new String[0], new String[0] );
		assertEquals( -1, left.compareTo( right, null ) );
		assertEquals( 1, right.compareTo( left, null ) );
	}

	@Test
	public void publicationTypeHasPrecedenceOverArticleType() {
		WebCmsArticleCondition left = condition( new String[0], new String[] { "xyz" }, new String[0] );
		WebCmsArticleCondition right = condition( new String[] { "123" }, new String[0], new String[0] );

		assertEquals( -1, left.compareTo( right, null ) );
		assertEquals( 1, right.compareTo( left, null ) );
	}

	@Test
	public void sameLengthAttributesAreConsideredEqualInPrecedence() {
		assertEquals( 0,
		              condition( new String[0], new String[0], new String[0] )
				              .compareTo( condition( new String[0], new String[0], new String[0] ), null )
		);
		assertEquals( 0,
		              condition( new String[] { "xyz" }, new String[] { "xyz" }, new String[] { "xyz" } )
				              .compareTo( condition( new String[] { "123" }, new String[] { "123" }, new String[] { "123" } ), null )
		);
	}

	@Test
	public void fewerAttributesConditionHasPrecedenceOverMoreAttributes() {
		WebCmsArticleCondition left = condition( new String[] { "xyz" }, new String[] { "xyz" }, new String[] { "xyz" } );
		WebCmsArticleCondition right = condition( new String[] { "xyz" }, new String[] { "xyz", "123" }, new String[] { "xyz" } );

		assertEquals( -1, left.compareTo( right, null ) );
		assertEquals( 1, right.compareTo( left, null ) );
	}

	private WebCmsArticleCondition condition( String[] articleTypes, String[] publicationTypes, String[] publications ) {
		WebCmsArticleCondition condition = new WebCmsArticleCondition( context, resolver );

		Map<String, Object> values = new HashMap<>();
		values.put( "articleType", articleTypes );
		values.put( "publicationType", publicationTypes );
		values.put( "publication", publications );

		AnnotatedElement annotatedElement = mock( AnnotatedElement.class );
		when( annotatedElement.getAnnotation( WebCmsArticleMapping.class ) )
				.thenReturn( AnnotationUtils.synthesizeAnnotation( values, WebCmsArticleMapping.class, null ) );
		condition.setAnnotatedElement( annotatedElement );

		return condition;
	}
}