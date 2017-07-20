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

import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.webcms.WebCmsModule;
import com.foreach.across.modules.webcms.data.WebCmsDataImportService;
import com.foreach.across.modules.webcms.domain.article.QWebCmsArticle;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticleRepository;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.across.test.AcrossWebAppConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@AcrossWebAppConfiguration
@TestPropertySource(properties = "acrossHibernate.createUnitOfWorkFactory=true")
public class ITArticleImporting
{
	@Autowired
	private WebCmsArticleRepository articleRepository;

	@Autowired
	private WebCmsDataImportService dataImportService;

	@Autowired
	private ApplicationContext applicationContext;

	@SuppressWarnings( "all" )
	@Test
	public void testArticleImporting() throws Exception {
		Yaml yaml = new Yaml();
		Map<String, Object> data = (Map<String, Object>) yaml.load( applicationContext.getResource( "classpath:installers/test-articles.yml" ).getInputStream() );
		dataImportService.importData( data );

		QWebCmsArticle query = QWebCmsArticle.webCmsArticle;

		WebCmsArticle article = articleRepository.findOne( query.publication.publicationKey.eq( "test-publication" )
		                                                                                   .and( query.title.eq( "Test article 1" ) ) );
		assertNotNull( article );

		WebCmsArticle other = articleRepository.findOne( query.publication.publicationKey.eq( "test-publication" )
		                                                                                 .and( query.title.eq( "Test article 2" ) ) );
		assertNotNull( other );
		assertNotEquals( article, other );
	}

	@AcrossTestConfiguration(modules = { WebCmsModule.NAME, AcrossHibernateJpaModule.NAME })
	protected static class Config
	{
	}
}
