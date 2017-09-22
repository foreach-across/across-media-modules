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

import com.foreach.across.modules.webcms.data.WebCmsDataImportService;
import com.foreach.across.modules.webcms.domain.article.QWebCmsArticle;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticleRepository;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticleTypeRepository;
import com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublicationRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.yaml.snakeyaml.Yaml;

import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
public class ITArticleImportAndCreation extends AbstractCmsApplicationIT
{
	@Autowired
	private WebCmsArticleRepository articleRepository;

	@Autowired
	private WebCmsDataImportService dataImportService;

	@Autowired
	private WebCmsComponentModelService componentModelService;

	@Autowired
	private WebCmsArticleTypeRepository articleTypeRepository;

	@Autowired
	private WebCmsPublicationRepository publicationRepository;

	@Autowired
	private ApplicationContext applicationContext;

	@SuppressWarnings("all")
	@Test
	public void publicationAndArticleImportingInSingleDataSetIsAllowed() throws Exception {
		Yaml yaml = new Yaml();
		Map<String, Object> data = (Map<String, Object>) yaml.load(
				applicationContext.getResource( "classpath:installers/test-articles.yml" ).getInputStream() );
		dataImportService.importData( data );

		QWebCmsArticle query = QWebCmsArticle.webCmsArticle;

		WebCmsArticle article = articleRepository.findOne( query.publication.publicationKey.eq( "test-publication" )
		                                                                                   .and( query.title.eq( "Test article 1" ) ) );
		assertNotNull( article );

		WebCmsArticle other = articleRepository.findOne( query.publication.publicationKey.eq( "test-publication" )
		                                                                                 .and( query.title.eq( "Test article 2" ) ) );
		assertNotNull( other );
		assertNotEquals( article, other );

		assertDefaultBodyComponent( article );
		assertDefaultBodyComponent( other );
	}

	@Test
	public void creatingArticleSetsComponentsAttachedToTheArticleType() throws Exception {
		WebCmsArticle blog = WebCmsArticle.builder()
		                                  .articleType( articleTypeRepository.findOneByTypeKeyAndDomain( "blog", WebCmsDomain.NONE ) )
		                                  .publication( publicationRepository.findOneByPublicationKeyAndDomain( "blogs", WebCmsDomain.NONE ) )
		                                  .title( "Blog article" )
		                                  .build();
		articleRepository.save( blog );

		assertDefaultBodyComponent( blog );
	}

	private void assertDefaultBodyComponent( WebCmsArticle article ) {
		Collection<WebCmsComponentModel> models = componentModelService.getComponentModelsForOwner( article, WebCmsDomain.NONE );
		assertEquals( 1, models.size() );

		WebCmsComponentModel content = models.iterator().next();
		assertEquals( "content", content.getName() );
		assertEquals( "Article content", content.getTitle() );
		assertTrue( content instanceof ContainerWebCmsComponentModel );

		ContainerWebCmsComponentModel container = (ContainerWebCmsComponentModel) content;
		assertEquals( 1, container.size() );
		TextWebCmsComponentModel body = container.getMember( "body", TextWebCmsComponentModel.class );
		assertNotNull( body );
		assertTrue( body.getContent().contains( "<h1>" + article.getTitle() + "</h1>" ) );
	}
}
