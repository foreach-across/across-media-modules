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

package com.foreach.across.modules.webcms.domain.domain.support;

import com.foreach.across.modules.entity.actions.EntityConfigurationAllowableActionsBuilder;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.spring.security.actions.AllowableActions;
import com.foreach.across.modules.webcms.WebCmsEntityAttributes;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainBound;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class TestCurrentDomainAwareAllowableActionsBuilder
{
	private WebCmsDomain currentDomain = WebCmsDomain.builder().id( 123L ).build();

	@Mock
	private EntityConfiguration entityConfiguration;

	@Mock
	private AllowableActions configurationActions;

	@Mock
	private AllowableActions entityActions;

	@Mock
	private WebCmsDomainBound entity;

	@Mock
	private WebCmsMultiDomainService multiDomainService;

	@Mock
	private EntityConfigurationAllowableActionsBuilder targetBuilder;

	@InjectMocks
	private CurrentDomainAwareAllowableActionsBuilder actionsBuilder;

	@Before
	public void before() {
		when( actionsBuilder.getAllowableActions( entityConfiguration ) ).thenReturn( configurationActions );
		when( actionsBuilder.getAllowableActions( entityConfiguration, entity ) ).thenReturn( entityActions );
		when( entityConfiguration.getEntityType() ).thenReturn( WebCmsDomainBound.class );
	}

	@Test
	public void domainBoundEntitiesCanNotBeManagedOnNoDomainIfTheyDontAcceptNoDomain() {
		when( multiDomainService.isDomainBound( WebCmsDomainBound.class ) ).thenReturn( true );
		when( multiDomainService.getCurrentDomain() ).thenReturn( WebCmsDomain.NONE );

		assertSame( CurrentDomainAwareAllowableActionsBuilder.NOTHING_ALLOWED, actionsBuilder.getAllowableActions( entityConfiguration ) );
		verifyNoMoreInteractions( targetBuilder );
	}

	@Test
	public void domainBoundEntitiesCanBeManagedOnNoDomainIfNoDomainIsAllowedAsWell() {
		when( multiDomainService.isDomainBound( WebCmsDomainBound.class ) ).thenReturn( true );
		when( multiDomainService.getCurrentDomain() ).thenReturn( WebCmsDomain.NONE );
		when( multiDomainService.isNoDomainAllowed( WebCmsDomainBound.class ) ).thenReturn( true );

		assertSame( configurationActions, actionsBuilder.getAllowableActions( entityConfiguration ) );
	}

	@Test
	public void domainBoundEntitiesCanBeManagedOnASpecificDomain() {
		when( multiDomainService.isDomainBound( WebCmsDomainBound.class ) ).thenReturn( true );
		when( multiDomainService.getCurrentDomain() ).thenReturn( currentDomain );

		assertSame( configurationActions, actionsBuilder.getAllowableActions( entityConfiguration ) );
	}

	@Test
	public void domainBoundEntitiesCanNotBeManagedOnSpecificDomainIfAttributeOverrides() {
		when( multiDomainService.isDomainBound( WebCmsDomainBound.class ) ).thenReturn( true );
		when( multiDomainService.getCurrentDomain() ).thenReturn( currentDomain );
		when( entityConfiguration.getAttribute( WebCmsEntityAttributes.ALLOW_PER_DOMAIN, Boolean.class ) ).thenReturn( false );

		assertSame( CurrentDomainAwareAllowableActionsBuilder.NOTHING_ALLOWED, actionsBuilder.getAllowableActions( entityConfiguration ) );
		verifyNoMoreInteractions( targetBuilder );
	}

	@Test
	public void notDomainBoundEntitiesCanNotBeManagedOnSpecificDomain() {
		when( multiDomainService.isDomainBound( WebCmsDomainBound.class ) ).thenReturn( false );
		when( multiDomainService.getCurrentDomain() ).thenReturn( currentDomain );

		assertSame( CurrentDomainAwareAllowableActionsBuilder.NOTHING_ALLOWED, actionsBuilder.getAllowableActions( entityConfiguration ) );
		verifyNoMoreInteractions( targetBuilder );
	}

	@Test
	public void notDomainBoundEntitiesCanBeManagedOnNoDomain() {
		when( multiDomainService.isDomainBound( WebCmsDomainBound.class ) ).thenReturn( false );
		when( multiDomainService.getCurrentDomain() ).thenReturn( WebCmsDomain.NONE );

		assertSame( configurationActions, actionsBuilder.getAllowableActions( entityConfiguration ) );
	}

	@Test
	public void notDomainBoundEntitiesCanBeManagedOnAnyDomainIfAttributePresent() {
		when( multiDomainService.isDomainBound( WebCmsDomainBound.class ) ).thenReturn( false );
		when( multiDomainService.getCurrentDomain() ).thenReturn( currentDomain );
		when( entityConfiguration.getAttribute( WebCmsEntityAttributes.ALLOW_PER_DOMAIN, Boolean.class ) ).thenReturn( true );

		assertSame( configurationActions, actionsBuilder.getAllowableActions( entityConfiguration ) );
	}

	@Test
	public void specificDomainBoundEntityCanBeManagedOnNoDomainIfItHasNoDomain() {
		when( multiDomainService.isDomainBound( entity ) ).thenReturn( true );
		when( multiDomainService.getCurrentDomain() ).thenReturn( WebCmsDomain.NONE );

		assertSame( entityActions, actionsBuilder.getAllowableActions( entityConfiguration, entity ) );
	}

	@Test
	public void specificDomainBoundEntityCanBeManagedOnDomainItBelongsTo() {
		when( multiDomainService.isDomainBound( entity ) ).thenReturn( true );
		when( multiDomainService.getCurrentDomain() ).thenReturn( currentDomain );
		when( entity.getDomain() ).thenReturn( currentDomain );

		assertSame( entityActions, actionsBuilder.getAllowableActions( entityConfiguration, entity ) );
	}

	@Test
	public void domainBoundEntityCanNotBeManagedOnNoDomainIfDomainSet() {
		when( multiDomainService.isDomainBound( entity ) ).thenReturn( true );
		when( multiDomainService.getCurrentDomain() ).thenReturn( WebCmsDomain.NONE );
		when( entity.getDomain() ).thenReturn( currentDomain );

		assertSame( CurrentDomainAwareAllowableActionsBuilder.NOTHING_ALLOWED, actionsBuilder.getAllowableActions( entityConfiguration, entity ) );
		verifyNoMoreInteractions( targetBuilder );
	}

	@Test
	public void domainBoundEntityCanNotBeManagedOnOtherDomain() {
		when( multiDomainService.isDomainBound( entity ) ).thenReturn( true );
		when( multiDomainService.getCurrentDomain() ).thenReturn( WebCmsDomain.builder().id( 456L ).build() );
		when( entity.getDomain() ).thenReturn( currentDomain );

		assertSame( CurrentDomainAwareAllowableActionsBuilder.NOTHING_ALLOWED, actionsBuilder.getAllowableActions( entityConfiguration, entity ) );
		verifyNoMoreInteractions( targetBuilder );
	}

	@Test
	public void entityWithNoDomainCanBeManagedOnAnyDomainIfAttributeOverrides() {
		when( multiDomainService.isDomainBound( entity ) ).thenReturn( true );
		when( multiDomainService.getCurrentDomain() ).thenReturn( currentDomain );
		when( entityConfiguration.getAttribute( WebCmsEntityAttributes.ALLOW_PER_DOMAIN, Boolean.class ) ).thenReturn( true );

		assertSame( entityActions, actionsBuilder.getAllowableActions( entityConfiguration, entity ) );
	}
}
