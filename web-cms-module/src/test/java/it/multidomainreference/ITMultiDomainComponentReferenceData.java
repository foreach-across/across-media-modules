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

import com.foreach.across.modules.webcms.domain.component.*;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainRepository;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageRepository;
import it.AbstractMultiDomainCmsApplicationWithTestDataIT;
import lombok.val;
import modules.multidomaintest.ui.CustomComponentMetadata;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

public class ITMultiDomainComponentReferenceData extends AbstractMultiDomainCmsApplicationWithTestDataIT
{
	@Autowired
	private WebCmsComponentRepository componentRepository;

	@Autowired
	private WebCmsDomainRepository domainRepository;

	@Autowired
	private WebCmsComponentTypeRepository componentTypeRepository;

	@Autowired
	private WebCmsPageRepository pageRepository;

	@Autowired
	private WebCmsComponentModelService componentModelService;

	@Test
	public void contentNlShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "nl-foreach" );
		WebCmsComponent component = componentRepository.findOneByOwnerObjectIdAndNameAndDomain( null, "content", domain );
		assertNotNull( component );
		assertEquals( "Content (NL)", component.getTitle() );
		assertEquals( componentModelService.getComponentType( "plain-text" ), component.getComponentType() );
		assertEquals( "Global component: content (NL)", component.getBody() );
	}

	@Test
	public void contentBeShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "be-foreach" );
		WebCmsComponent component = componentRepository.findOneByOwnerObjectIdAndNameAndDomain( null, "content", domain );
		assertNotNull( component );
		assertEquals( "Content (BE)", component.getTitle() );
		assertEquals( componentModelService.getComponentType( "plain-text" ), component.getComponentType() );
		assertEquals( "Global component: content (BE)", component.getBody() );
	}

	@Test
	public void contentDeShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "de-foreach" );
		WebCmsComponent component = componentRepository.findOneByOwnerObjectIdAndNameAndDomain( null, "content", domain );
		assertNotNull( component );
		assertEquals( "Content (DE)", component.getTitle() );
		assertEquals( componentModelService.getComponentType( "plain-text" ), component.getComponentType() );
		assertEquals( "Global component: content (DE)", component.getBody() );
	}

	@Test
	public void teaserNlShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "nl-foreach" );
		WebCmsComponent component = componentRepository.findOneByOwnerObjectIdAndNameAndDomain( null, "nl-teaser", domain );
		List<WebCmsComponent> children = componentRepository.findAllByOwnerObjectIdAndDomainOrderBySortIndexAsc( component.getObjectId(),
		                                                                                                         component.getDomain() );
		assertNotNull( component );
		assertEquals( componentModelService.getComponentType( "nl-foreach:teaser", domain ), component.getComponentType() );
		assertEquals( 4, children.size() );
		assertEquals( "Teaser title", children.get( 0 ).getBody() );
		assertEquals( "Sample teaser header (NL)", children.get( 1 ).getBody() );
		assertEquals( "Sample teaser body (NL)", children.get( 2 ).getBody() );
		assertEquals( "Sample teaser footer (NL)", children.get( 3 ).getBody() );
	}

	@Test
	public void teaserBeShouldHaveBeenDeleted() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "be-foreach" );
		WebCmsComponent component = componentRepository.findOneByOwnerObjectIdAndNameAndDomain( null, "be-teaser", domain );
		assertNull( component );
	}

	@Test
	public void teaserDeShouldHaveBeenImportedAndExtended() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "de-foreach" );
		WebCmsComponent component = componentRepository.findOneByOwnerObjectIdAndNameAndDomain( null, "de-teaser", domain );
		List<WebCmsComponent> children = componentRepository.findAllByOwnerObjectIdAndDomainOrderBySortIndexAsc( component.getObjectId(),
		                                                                                                         component.getDomain() );
		assertNotNull( component );
		assertEquals( componentModelService.getComponentType( "de-foreach:teaser", domain ), component.getComponentType() );
		assertEquals( 4, children.size() );
		assertEquals( "Teaser title", children.get( 0 ).getBody() );
		assertEquals( "Sample teaser header (DE)", children.get( 1 ).getBody() );
		assertEquals( "Sample teaser body (DE)", children.get( 2 ).getBody() );
		assertEquals( "Sample teaser footer (DE)", children.get( 3 ).getBody() );
	}

	@Test
	public void teaserFrShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "fr-foreach" );
		WebCmsComponent component = componentRepository.findOneByOwnerObjectIdAndNameAndDomain( null, "fr-teaser", domain );
		List<WebCmsComponent> children = componentRepository.findAllByOwnerObjectIdAndDomainOrderBySortIndexAsc( component.getObjectId(),
		                                                                                                         component.getDomain() );
		assertNotNull( component );
		assertEquals( componentModelService.getComponentType( "teaser", domain ), component.getComponentType() );
		assertEquals( 4, children.size() );
		assertEquals( "Teaser title", children.get( 0 ).getBody() );
		assertEquals( "Sample teaser header (FR)", children.get( 1 ).getBody() );
		assertEquals( "Sample teaser body (FR)", children.get( 2 ).getBody() );
		assertEquals( "Sample teaser footer (FR)", children.get( 3 ).getBody() );
	}

	@Test
	public void homepageNlShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "nl-foreach" );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/homepage", domain );
		assertNotNull( page );
		List<WebCmsComponent> components = componentRepository.findAllByOwnerObjectIdAndDomainOrderBySortIndexAsc( page.getObjectId(), page.getDomain() );
		assertEquals( 1, components.size() );
		assertComponent( components.get( 0 ), "header", "Header content...", domain );
	}

	@Test
	public void acrossBlogTypeShouldHaveBeenImported() {
		val query = QWebCmsComponentType.webCmsComponentType;
		WebCmsComponentType componentType = componentTypeRepository.findOne( query.typeKey.eq( "ax-blog" ).and( query.domain.isNull() ) );
		assertNotNull( componentType );
		assertEquals( "Across Blog", componentType.getName() );
	}

	@Test
	public void acrossBlogBeShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "be-foreach" );
		WebCmsComponent blog = componentRepository.findOneByOwnerObjectIdAndNameAndDomain( null, "ax-blog-be", domain );
		assertNotNull( blog );
		List<WebCmsComponent> children = componentRepository.findAllByOwnerObjectIdAndDomainOrderBySortIndexAsc( blog.getObjectId(), blog.getDomain() );
		assertEquals( 4, children.size() );
		assertEquals( "Thinking Across", children.get( 0 ).getBody() );
		assertEquals( "Multi-domain support for WebCmsModule has been released (BE)", children.get( 1 ).getBody() );
		assertEquals( "Lorem ipsum (BE)", children.get( 2 ).getBody() );
		assertEquals( "Start with across using start-across.foreach.be", children.get( 3 ).getBody() );
	}

	@Test
	public void acrossBlogDeShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "de-foreach" );
		WebCmsComponent blog = componentRepository.findOneByOwnerObjectIdAndNameAndDomain( null, "ax-blog-de", domain );
		assertNotNull( blog );
		List<WebCmsComponent> children = componentRepository.findAllByOwnerObjectIdAndDomainOrderBySortIndexAsc( blog.getObjectId(), blog.getDomain() );
		assertEquals( 4, children.size() );
		assertEquals( "Thinking Across", children.get( 0 ).getBody() );
		assertEquals( "Multi-domain support for WebCmsModule has been released (DE)", children.get( 1 ).getBody() );
		assertEquals( "Lorem ipsum (DE)", children.get( 2 ).getBody() );
		assertEquals( "Start with across using start-across.foreach.de", children.get( 3 ).getBody() );
	}

	@Test
	public void acrossBlogNlShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "nl-foreach" );
		WebCmsComponent blog = componentRepository.findOneByOwnerObjectIdAndNameAndDomain( null, "ax-blog-nl", domain );
		assertNotNull( blog );
		List<WebCmsComponent> children = componentRepository.findAllByOwnerObjectIdAndDomainOrderBySortIndexAsc( blog.getObjectId(), blog.getDomain() );
		assertEquals( 4, children.size() );
		assertEquals( "Thinking Across", children.get( 0 ).getBody() );
		assertEquals( "Multi-domain support for WebCmsModule has been released (NL)", children.get( 1 ).getBody() );
		assertEquals( "Lorem ipsum (NL)", children.get( 2 ).getBody() );
		assertEquals( "Start with across using start-across.foreach.nl", children.get( 3 ).getBody() );
	}

	@Test
	public void myCustomComponentTypeShouldHaveBeenImported() {
		WebCmsComponentType componentType = componentTypeRepository.findOneByTypeKeyAndDomain( "my-custom-component", WebCmsDomain.NONE );
		assertNotNull( componentType );
		assertEquals( "modules.multidomaintest.ui.CustomComponentMetadata", componentType.getAttribute( "metadata" ) );
	}

	@Test
	public void myCustomComponentShouldHaveBeenImported() {
		WebCmsComponent component = componentRepository.findOneByOwnerObjectIdAndNameAndDomain( null, "my-custom-component", null );
		assertNotNull( component );
		WebCmsComponentModel componentModel = componentModelService.getComponentModel( component.getObjectId() );
		assertNotNull( componentModel );
		CustomComponentMetadata metadata = componentModel.getMetadata( CustomComponentMetadata.class );
		assertEquals( "My Custom Title", metadata.getTitle() );
		assertEquals( "blue", metadata.getBackgroundColor() );
		assertFalse( metadata.getActive() );
		assertEquals( 2, metadata.getZIndex().intValue() );
	}

	private void assertComponent( WebCmsComponent comp, String name, String body, WebCmsDomain domain ) {
		assertNotNull( comp );
		assertEquals( name, comp.getName() );
		assertEquals( body, comp.getBody() );
		assertEquals( domain, comp.getDomain() );
	}
}
