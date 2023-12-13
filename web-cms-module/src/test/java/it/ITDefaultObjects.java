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

package it;

import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.webcms.WebCmsModule;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticleType;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticleTypeRepository;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentTypeRepository;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.page.QWebCmsPageType;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.WebCmsPageType;
import com.foreach.across.modules.webcms.domain.page.WebCmsPageTypeRepository;
import com.foreach.across.modules.webcms.domain.publication.*;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierLink;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierLinkRepository;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierRepository;
import com.foreach.across.test.AcrossTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.foreach.across.modules.webcms.domain.article.QWebCmsArticleType.webCmsArticleType;
import static com.foreach.across.modules.webcms.domain.component.QWebCmsComponentType.webCmsComponentType;
import static com.foreach.across.modules.webcms.domain.publication.QWebCmsPublicationType.webCmsPublicationType;
import static com.foreach.across.test.support.AcrossTestBuilders.web;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
class ITDefaultObjects
{
	@Test
	void byDefaultTheAssetsShouldBeImported() {
		try (AcrossTestContext ctx = web().modules( WebCmsModule.NAME, AcrossHibernateJpaModule.NAME )
		                                  .build()) {
			verifyComponentTypes( ctx );
			verifyPageTypes( ctx );
			verifyArticleTypes( ctx );
			verifyPublicationTypes( ctx );
			verifyPublications( ctx );
		}
	}

	private void verifyComponentTypes( AcrossContextBeanRegistry beanRegistry ) {
		WebCmsTypeSpecifierRepository typeSpecifierRepository = beanRegistry.getBeanOfType( WebCmsTypeSpecifierRepository.class );
		WebCmsComponentTypeRepository componentTypeRepository = beanRegistry.getBeanOfType( WebCmsComponentTypeRepository.class );

		{
			val attr = new HashMap<String, String>();
			attr.put( TextWebCmsComponentModel.Attributes.TYPE, "proxy" );
			attr.put( WebCmsComponentType.COMPONENT_RESTRICTED, "true" );

			verifyComponentType(
					componentTypeRepository,
					typeSpecifierRepository,
					"wcm:type:component:proxy",
					"Proxy component",
					"proxy",
					attr
			);
		}

		{
			val attr = new HashMap<String, String>();
			attr.put( TextWebCmsComponentModel.Attributes.TYPE, "placeholder" );
			attr.put( WebCmsComponentType.COMPONENT_RESTRICTED, "true" );

			verifyComponentType(
					componentTypeRepository,
					typeSpecifierRepository,
					"wcm:type:component:placeholder",
					"Placeholder",
					"placeholder",
					attr
			);
		}

		{
			val attr = new HashMap<String, String>();
			attr.put( TextWebCmsComponentModel.Attributes.TYPE, "container" );

			verifyComponentType(
					componentTypeRepository,
					typeSpecifierRepository,
					"wcm:type:component:container",
					"Container",
					"container",
					attr
			);
		}

		{
			val attr = new HashMap<String, String>();
			attr.put( TextWebCmsComponentModel.Attributes.TYPE, "fixed-container" );
			attr.put( WebCmsComponentType.COMPONENT_RESTRICTED, "true" );

			verifyComponentType(
					componentTypeRepository,
					typeSpecifierRepository,
					"wcm:type:component:fixed-container",
					"Fixed container",
					"fixed-container",
					attr
			);
		}

		{
			val attr = new HashMap<String, String>();
			attr.put( TextWebCmsComponentModel.Attributes.TYPE, "plain-text" );
			attr.put( TextWebCmsComponentModel.Attributes.MULTI_LINE, "false" );

			verifyComponentType(
					componentTypeRepository,
					typeSpecifierRepository,
					"wcm:type:component:text-field",
					"Text field",
					"text-field",
					attr
			);
		}

		{
			val attr = new HashMap<String, String>();
			attr.put( TextWebCmsComponentModel.Attributes.TYPE, "rich-text" );
			attr.put( TextWebCmsComponentModel.Attributes.PROFILE, "rich-text" );

			verifyComponentType(
					componentTypeRepository,
					typeSpecifierRepository,
					"wcm:type:component:rich-text",
					"Rich text",
					"rich-text",
					attr
			);
		}

		{
			val attr = new HashMap<String, String>();
			attr.put( TextWebCmsComponentModel.Attributes.TYPE, "plain-text" );
			attr.put( TextWebCmsComponentModel.Attributes.ROWS, "1" );

			verifyComponentType(
					componentTypeRepository,
					typeSpecifierRepository,
					"wcm:type:component:plain-text",
					"Plain text",
					"plain-text",
					attr
			);
		}

		verifyComponentType(
				componentTypeRepository,
				typeSpecifierRepository,
				"wcm:type:component:html",
				"HTML",
				"html",
				Collections.singletonMap( "type", "markup" )
		);
	}

	private void verifyComponentType( WebCmsComponentTypeRepository componentTypeRepository, WebCmsTypeSpecifierRepository typeSpecifierRepository,
	                                  String objectId, String name, String typeKey, Map<String, String> attributes ) {
		WebCmsComponentType componentType = componentTypeRepository.findOne( webCmsComponentType.objectId.eq( objectId ) ).orElse( null );
		assertNotNull( componentType );
		assertEquals( objectId, componentType.getObjectId() );
		assertEquals( "component", componentType.getObjectType() );
		assertEquals( name, componentType.getName() );
		assertEquals( typeKey, componentType.getTypeKey() );
		assertNotNull( componentType.getDescription() );
		assertEquals( attributes, new HashMap<>( componentType.getAttributes() ) );
		assertEquals( Optional.of( componentType ), typeSpecifierRepository.findOneByObjectTypeAndTypeKeyAndDomain( "component", typeKey, WebCmsDomain.NONE ) );
	}

	private void verifyPageTypes( AcrossContextBeanRegistry beanRegistry ) {
		WebCmsTypeSpecifierRepository typeSpecifierRepository = beanRegistry.getBeanOfType( WebCmsTypeSpecifierRepository.class );
		WebCmsPageTypeRepository pageTypeRepository = beanRegistry.getBeanOfType( WebCmsPageTypeRepository.class );

		WebCmsPageType defaultPageType = pageTypeRepository.findOne( QWebCmsPageType.webCmsPageType.objectId.eq( "wcm:type:page:default" ) ).orElse( null );
		assertNotNull( defaultPageType );
		assertEquals( "wcm:type:page:default", defaultPageType.getObjectId() );
		assertEquals( "page", defaultPageType.getObjectType() );
		assertEquals( "Default", defaultPageType.getName() );
		assertEquals( "default", defaultPageType.getTypeKey() );
		assertTrue( defaultPageType.isPublishable() );
		assertTrue( defaultPageType.hasEndpoint() );
		assertEquals( Optional.of( defaultPageType ), typeSpecifierRepository.findOneByObjectTypeAndTypeKeyAndDomain( "page", "default", WebCmsDomain.NONE ) );

		WebCmsPageType templatePageType = pageTypeRepository.findOne( QWebCmsPageType.webCmsPageType.objectId.eq( "wcm:type:page:template" ) ).orElse( null );
		assertNotNull( templatePageType );
		assertEquals( "wcm:type:page:template", templatePageType.getObjectId() );
		assertEquals( "page", templatePageType.getObjectType() );
		assertEquals( "Template", templatePageType.getName() );
		assertEquals( "template", templatePageType.getTypeKey() );
		assertFalse( templatePageType.isPublishable() );
		assertFalse( templatePageType.hasEndpoint() );
		assertEquals( Optional.of( templatePageType ),
		              typeSpecifierRepository.findOneByObjectTypeAndTypeKeyAndDomain( "page", "template", WebCmsDomain.NONE ) );
	}

	private void verifyArticleTypes( AcrossContextBeanRegistry beanRegistry ) {
		WebCmsTypeSpecifierRepository typeSpecifierRepository = beanRegistry.getBeanOfType( WebCmsTypeSpecifierRepository.class );
		WebCmsArticleTypeRepository articleTypeRepository = beanRegistry.getBeanOfType( WebCmsArticleTypeRepository.class );

		WebCmsArticleType newsType = articleTypeRepository.findOne( webCmsArticleType.objectId.eq( "wcm:type:article:news" ) ).orElse( null );
		assertNotNull( newsType );
		assertEquals( "wcm:type:article:news", newsType.getObjectId() );
		assertEquals( "article", newsType.getObjectType() );
		assertEquals( "News", newsType.getName() );
		assertEquals( "news", newsType.getTypeKey() );
		assertEquals( "blog", newsType.getAttribute( "parent" ) );
		assertEquals( Optional.of( newsType ), typeSpecifierRepository.findOneByObjectTypeAndTypeKeyAndDomain( "article", "news", WebCmsDomain.NONE ) );

		WebCmsArticleType blogType = articleTypeRepository.findOne( webCmsArticleType.objectId.eq( "wcm:type:article:blog" ) ).orElse( null );
		assertNotNull( blogType );
		assertEquals( "wcm:type:article:blog", blogType.getObjectId() );
		assertEquals( "article", blogType.getObjectType() );
		assertEquals( "Blog", blogType.getName() );
		assertEquals( "blog", blogType.getTypeKey() );
		assertNull( blogType.getAttribute( "parent" ) );
		assertEquals( Optional.of( blogType ), typeSpecifierRepository.findOneByObjectTypeAndTypeKeyAndDomain( "article", "blog", WebCmsDomain.NONE ) );
	}

	private void verifyPublicationTypes( AcrossContextBeanRegistry beanRegistry ) {
		WebCmsTypeSpecifierRepository typeSpecifierRepository = beanRegistry.getBeanOfType( WebCmsTypeSpecifierRepository.class );
		WebCmsPublicationTypeRepository publicationTypeRepository = beanRegistry.getBeanOfType( WebCmsPublicationTypeRepository.class );
		WebCmsTypeSpecifierLinkRepository typeSpecifierLinkRepository = beanRegistry.getBeanOfType( WebCmsTypeSpecifierLinkRepository.class );

		WebCmsPublicationType newsType = publicationTypeRepository.findOne( webCmsPublicationType.objectId.eq( "wcm:type:publication:news" ) ).orElse( null );
		assertNotNull( newsType );
		assertEquals( "wcm:type:publication:news", newsType.getObjectId() );
		assertEquals( "publication", newsType.getObjectType() );
		assertEquals( "News", newsType.getName() );
		assertEquals( "news", newsType.getTypeKey() );
		assertEquals( Optional.of( newsType ), typeSpecifierRepository.findOneByObjectTypeAndTypeKeyAndDomain( "publication", "news", WebCmsDomain.NONE ) );

		assertEquals(
				Collections.singletonList( typeSpecifierRepository.findOneByObjectTypeAndTypeKeyAndDomain( WebCmsArticleType.OBJECT_TYPE, "news",
				                                                                                           WebCmsDomain.NONE ).orElse( null ) ),
				typeSpecifierLinkRepository.findAllByOwnerObjectIdAndLinkTypeOrderBySortIndexAsc( newsType.getObjectId(), WebCmsArticleType.OBJECT_TYPE )
				                           .stream()
				                           .map( WebCmsTypeSpecifierLink::getTypeSpecifier )
				                           .collect( Collectors.toList() )
		);

		WebCmsPublicationType blogType = publicationTypeRepository.findOne( webCmsPublicationType.objectId.eq( "wcm:type:publication:blog" ) ).orElse( null );
		assertNotNull( blogType );
		assertEquals( "wcm:type:publication:blog", blogType.getObjectId() );
		assertEquals( "publication", blogType.getObjectType() );
		assertEquals( "Blog", blogType.getName() );
		assertEquals( "blog", blogType.getTypeKey() );
		assertEquals( Optional.of( blogType ), typeSpecifierRepository.findOneByObjectTypeAndTypeKeyAndDomain( "publication", "blog", WebCmsDomain.NONE ) );

		assertEquals(
				Collections.singletonList( typeSpecifierRepository.findOneByObjectTypeAndTypeKeyAndDomain( WebCmsArticleType.OBJECT_TYPE, "blog",
				                                                                                           WebCmsDomain.NONE ).orElse( null ) ),
				typeSpecifierLinkRepository.findAllByOwnerObjectIdAndLinkTypeOrderBySortIndexAsc( blogType.getObjectId(), WebCmsArticleType.OBJECT_TYPE )
				                           .stream()
				                           .map( WebCmsTypeSpecifierLink::getTypeSpecifier )
				                           .collect( Collectors.toList() )
		);
	}

	private void verifyPublications( AcrossContextBeanRegistry beanRegistry ) {
		WebCmsPublicationRepository publicationRepository = beanRegistry.getBeanOfType( WebCmsPublicationRepository.class );
		WebCmsPublicationTypeRepository publicationTypeRepository = beanRegistry.getBeanOfType( WebCmsPublicationTypeRepository.class );

		WebCmsPublication news = publicationRepository.findOne( QWebCmsPublication.webCmsPublication.objectId.eq( "wcm:asset:publication:news" ) )
		                                              .orElse( null );
		assertEquals( "wcm:asset:publication:news", news.getObjectId() );
		assertEquals( "News", news.getName() );
		assertEquals( "news", news.getPublicationKey() );
		assertEquals( publicationTypeRepository.findOneByTypeKeyAndDomain( "news", WebCmsDomain.NONE ), Optional.of( news.getPublicationType() ) );

		WebCmsPage newsTemplatePage = news.getArticleTemplatePage();
		assertNotNull( newsTemplatePage );
		assertEquals( "/news/*", newsTemplatePage.getCanonicalPath() );
		assertEquals( "template", newsTemplatePage.getPageType().getTypeKey() );

		WebCmsPublication blogs = publicationRepository.findOne( QWebCmsPublication.webCmsPublication.objectId.eq( "wcm:asset:publication:blogs" ) )
		                                               .orElse( null );
		assertEquals( "wcm:asset:publication:blogs", blogs.getObjectId() );
		assertEquals( "Blogs", blogs.getName() );
		assertEquals( "blogs", blogs.getPublicationKey() );
		assertEquals( publicationTypeRepository.findOneByTypeKeyAndDomain( "blog", WebCmsDomain.NONE ), Optional.of( blogs.getPublicationType() ) );

		WebCmsPage blogTemplatePage = blogs.getArticleTemplatePage();
		assertNotNull( blogTemplatePage );
		assertEquals( "/blog/*", blogTemplatePage.getCanonicalPath() );
		assertEquals( "template", blogTemplatePage.getPageType().getTypeKey() );
	}

	@Test
	void disablingTheDefaultAssetsInstaller() {
		try (AcrossTestContext ctx = web().modules( WebCmsModule.NAME, AcrossHibernateJpaModule.NAME )
		                                  .property( "web-cms-module.default-data.assets.enabled", "false" )
		                                  .build()) {
			WebCmsPublicationRepository publicationRepository = ctx.getBeanOfType( WebCmsPublicationRepository.class );
			assertEquals( 0, publicationRepository.count() );
		}
	}
}
