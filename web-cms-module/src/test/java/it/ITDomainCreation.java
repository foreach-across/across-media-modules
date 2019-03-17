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

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
public class ITDomainCreation extends AbstractCmsApplicationIT
{
	@Autowired
	private WebCmsDomainRepository domainRepository;

	@Test
	void createAndFetchDomain() {
		WebCmsDomain domain = WebCmsDomain.builder()
		                                  .domainKey( "manually-created-domain" )
		                                  .name( "Manually created domain" )
		                                  .description( "Some manually created domain..." )
		                                  .attribute( "dns", "manual.domain" )
		                                  .build();

		domainRepository.save( domain );
		assertFalse( domain.isNew() );

		WebCmsDomain byId = domainRepository.findOne( domain.getId() );
		assertEquals( domain, byId );

		WebCmsDomain byKey = domainRepository.findOneByDomainKey( domain.getDomainKey() );
		assertEquals( domain, byKey );

		WebCmsDomain byObjectId = domainRepository.findOneByObjectId( domain.getObjectId() );
		assertEquals( domain, byObjectId );

		assertEquals( "manually-created-domain", byObjectId.getDomainKey() );
		assertEquals( "wcm:domain:manually-created-domain", byObjectId.getObjectId() );
		assertEquals( "Manually created domain", byObjectId.getName() );
		assertEquals( "Some manually created domain...", byObjectId.getDescription() );
		assertEquals( domain.getAttributes(), byObjectId.getAttributes() );
		assertEquals( "manual.domain", byObjectId.getAttribute( "dns" ) );
		assertTrue( byObjectId.isActive() );

		domainRepository.save( byObjectId.toBuilder().active( false ).build() );

		WebCmsDomain updated = domainRepository.findOne( domain.getId() );
		assertFalse( updated.isActive() );

		domainRepository.delete( updated.getId() );

		assertNull( domainRepository.findOneByDomainKey( domain.getDomainKey() ) );
	}
}
