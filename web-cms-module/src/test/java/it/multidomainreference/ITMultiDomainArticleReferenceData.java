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

package it.multidomainreference;

import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticleRepository;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticleType;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticleTypeRepository;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentRepository;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainRepository;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublicationRepository;
import it.AbstractMultiDomainCmsApplicationWithTestDataIT;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class ITMultiDomainArticleReferenceData extends AbstractMultiDomainCmsApplicationWithTestDataIT
{
	@Autowired
	private WebCmsArticleRepository articleRepository;

	@Autowired
	private WebCmsDomainRepository domainRepository;

	@Autowired
	private WebCmsArticleTypeRepository articleTypeRepository;

	@Autowired
	private WebCmsPublicationRepository publicationRepository;

	@Autowired
	private WebCmsComponentRepository componentRepository;

	@Test
	public void devTalksNlShouldHaveBeenDeleted() {
		WebCmsArticle article = articleRepository.findOneByObjectId( "wcm:asset:article:devtalks-nl" ).orElse( null );
		assertNull( article );
	}

	@Test
	public void devTalksDeShouldHaveBeenImported() {
		WebCmsArticle article = articleRepository.findOneByObjectId( "wcm:asset:article:devtalks-de" ).orElse( null );
		assertNotNull( article );
		assertEquals( "DevTalks Maart (DE)", article.getTitle() );
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "de-foreach" ).orElse( null );
		assertEquals( articleTypeRepository.findOneByTypeKeyAndDomain( "blog", WebCmsDomain.NONE ), Optional.of( article.getArticleType() ) );
		assertEquals( publicationRepository.findOneByObjectId( "wcm:asset:publication:devtalks-de" ), Optional.of( article.getPublication() ) );
		assertEquals( domain, article.getDomain() );
	}

	@Test
	public void devTalksBeShouldHaveBeenImportedAndExtended() {
		WebCmsArticle article = articleRepository.findOneByObjectId( "wcm:asset:article:devtalks-be" ).orElse( null );
		assertNotNull( article );
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "be-foreach" ).orElse( null );
		assertNotNull( domain );
		assertEquals( "DevTalks Maart (BE)", article.getTitle() );
		assertEquals( articleTypeRepository.findOneByTypeKeyAndDomain( "blog", WebCmsDomain.NONE ), Optional.of( article.getArticleType() ) );
		assertEquals( publicationRepository.findOneByObjectId( "wcm:asset:publication:devtalks-be" ), Optional.of( article.getPublication() ) );
		assertEquals( domain, article.getDomain() );
	}

	@Test
	public void bigArticleArticleTypeShouldHaveBeenImportedAndExtended() {
		WebCmsArticleType bigArticle = articleTypeRepository.findOneByObjectId( "wcm:type:article:big-article" ).orElse( null );
		assertNotNull( bigArticle );
		assertEquals( domainRepository.findOneByDomainKey( "be-foreach" ), Optional.of( bigArticle.getDomain() ) );
	}

	@Test
	public void webCmsMultiDomArticleShouldHaveBeenImported() {
		WebCmsArticle article = articleRepository.findOneByObjectId( "wcm:asset:article:webcms-multi-dom" ).orElse( null );
		assertNotNull( article );
		List<WebCmsComponent> components = componentRepository.findAllByOwnerObjectIdAndDomainOrderBySortIndexAsc( article.getObjectId(), article.getDomain() );
		assertEquals( 1, components.size() );
		assertEquals( article.getDomain(), components.get( 0 ).getDomain() );
	}

	@Test
	public void webCmsMultiDomBeArticleShouldHaveBeenImported() {
		WebCmsArticle article = articleRepository.findOneByObjectId( "wcm:asset:article:webcms-multi-dom-be" ).orElse( null );
		assertNotNull( article );
		List<WebCmsComponent> components = componentRepository.findAllByOwnerObjectIdAndDomainOrderBySortIndexAsc( article.getObjectId(), article.getDomain() );
		assertEquals( 1, components.size() );
		assertEquals( article.getDomain(), components.get( 0 ).getDomain() );
	}
}
