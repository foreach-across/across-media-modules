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
import com.foreach.across.modules.webcms.domain.publication.*;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierRepository;
import com.foreach.across.test.AcrossTestContext;
import lombok.val;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.foreach.across.modules.webcms.domain.article.QWebCmsArticleType.webCmsArticleType;
import static com.foreach.across.modules.webcms.domain.component.QWebCmsComponentType.webCmsComponentType;
import static com.foreach.across.modules.webcms.domain.publication.QWebCmsPublicationType.webCmsPublicationType;
import static com.foreach.across.test.support.AcrossTestBuilders.web;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class ITDefaultObjects
{
	@Test
	public void byDefaultTheAssetsShouldBeImported() {
		try (AcrossTestContext ctx = web().modules( WebCmsModule.NAME, AcrossHibernateJpaModule.NAME )
		                                  .build()) {
			verifyComponentTypes( ctx );
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
		WebCmsComponentType componentType = componentTypeRepository.findOne( webCmsComponentType.objectId.eq( objectId ) );
		assertNotNull( componentType );
		assertEquals( objectId, componentType.getObjectId() );
		assertEquals( "component", componentType.getObjectType() );
		assertEquals( name, componentType.getName() );
		assertEquals( typeKey, componentType.getTypeKey() );
		assertNotNull( componentType.getDescription() );
		assertEquals( attributes, new HashMap<>( componentType.getAttributes() ) );
		assertEquals( componentType, typeSpecifierRepository.findOneByObjectTypeAndTypeKey( "component", typeKey ) );
	}

	private void verifyArticleTypes( AcrossContextBeanRegistry beanRegistry ) {
		WebCmsTypeSpecifierRepository typeSpecifierRepository = beanRegistry.getBeanOfType( WebCmsTypeSpecifierRepository.class );
		WebCmsArticleTypeRepository articleTypeRepository = beanRegistry.getBeanOfType( WebCmsArticleTypeRepository.class );

		WebCmsArticleType newsType = articleTypeRepository.findOne( webCmsArticleType.objectId.eq( "wcm:type:article:news" ) );
		assertNotNull( newsType );
		assertEquals( "wcm:type:article:news", newsType.getObjectId() );
		assertEquals( "article", newsType.getObjectType() );
		assertEquals( "News", newsType.getName() );
		assertEquals( "news", newsType.getTypeKey() );
		assertEquals( newsType, typeSpecifierRepository.findOneByObjectTypeAndTypeKey( "article", "news" ) );

		WebCmsArticleType blogType = articleTypeRepository.findOne( webCmsArticleType.objectId.eq( "wcm:type:article:blog" ) );
		assertNotNull( blogType );
		assertEquals( "wcm:type:article:blog", blogType.getObjectId() );
		assertEquals( "article", blogType.getObjectType() );
		assertEquals( "Blog", blogType.getName() );
		assertEquals( "blog", blogType.getTypeKey() );
		assertEquals( blogType, typeSpecifierRepository.findOneByObjectTypeAndTypeKey( "article", "blog" ) );
	}

	private void verifyPublicationTypes( AcrossContextBeanRegistry beanRegistry ) {
		WebCmsTypeSpecifierRepository typeSpecifierRepository = beanRegistry.getBeanOfType( WebCmsTypeSpecifierRepository.class );
		WebCmsPublicationTypeRepository publicationTypeRepository = beanRegistry.getBeanOfType( WebCmsPublicationTypeRepository.class );

		WebCmsPublicationType newsType = publicationTypeRepository.findOne( webCmsPublicationType.objectId.eq( "wcm:type:publication:news" ) );
		assertNotNull( newsType );
		assertEquals( "wcm:type:publication:news", newsType.getObjectId() );
		assertEquals( "publication", newsType.getObjectType() );
		assertEquals( "News", newsType.getName() );
		assertEquals( "news", newsType.getTypeKey() );
		assertEquals( newsType, typeSpecifierRepository.findOneByObjectTypeAndTypeKey( "publication", "news" ) );

		WebCmsPublicationType blogType = publicationTypeRepository.findOne( webCmsPublicationType.objectId.eq( "wcm:type:publication:blog" ) );
		assertNotNull( blogType );
		assertEquals( "wcm:type:publication:blog", blogType.getObjectId() );
		assertEquals( "publication", blogType.getObjectType() );
		assertEquals( "Blog", blogType.getName() );
		assertEquals( "blog", blogType.getTypeKey() );
		assertEquals( blogType, typeSpecifierRepository.findOneByObjectTypeAndTypeKey( "publication", "blog" ) );
	}

	private void verifyPublications( AcrossContextBeanRegistry beanRegistry ) {
		WebCmsPublicationRepository publicationRepository = beanRegistry.getBeanOfType( WebCmsPublicationRepository.class );
		WebCmsPublicationTypeRepository publicationTypeRepository = beanRegistry.getBeanOfType( WebCmsPublicationTypeRepository.class );

		WebCmsPublication news = publicationRepository.findOne( QWebCmsPublication.webCmsPublication.objectId.eq( "wcm:asset:publication:news" ) );
		assertEquals( "wcm:asset:publication:news", news.getObjectId() );
		assertEquals( "News", news.getName() );
		assertEquals( "news", news.getPublicationKey() );
		assertEquals( publicationTypeRepository.findOneByTypeKey( "news" ), news.getPublicationType() );

		WebCmsPublication blogs = publicationRepository.findOne( QWebCmsPublication.webCmsPublication.objectId.eq( "wcm:asset:publication:blogs" ) );
		assertEquals( "wcm:asset:publication:blogs", blogs.getObjectId() );
		assertEquals( "Blogs", blogs.getName() );
		assertEquals( "blogs", blogs.getPublicationKey() );
		assertEquals( publicationTypeRepository.findOneByTypeKey( "blog" ), blogs.getPublicationType() );
	}

	@Test
	public void disablingTheDefaultAssetsInstaller() {
		try (AcrossTestContext ctx = web().modules( WebCmsModule.NAME, AcrossHibernateJpaModule.NAME )
		                                  .property( "webCmsModule.default-data.assets.enabled", "false" )
		                                  .build()) {
			WebCmsPublicationRepository publicationRepository = ctx.getBeanOfType( WebCmsPublicationRepository.class );
			assertEquals( 0, publicationRepository.count() );
		}
	}
}
