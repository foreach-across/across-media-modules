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

package com.foreach.across.modules.webcms.domain.domain;

import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.EntityRegistry;
import com.foreach.across.modules.spring.security.actions.AllowableAction;
import com.foreach.across.modules.spring.security.actions.AllowableActions;
import com.foreach.across.modules.webcms.domain.domain.config.WebCmsMultiDomainConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsMultiDomainAdminUiService
{
	private WebCmsDomain one = WebCmsDomain.builder().id( 123L ).build();
	private WebCmsDomain two = WebCmsDomain.builder().id( 456L ).build();

	@Mock
	private EntityConfiguration entityConfiguration;

	@Mock
	private EntityRegistry entityRegistry;

	@Mock
	private WebCmsMultiDomainConfiguration multiDomainConfiguration;

	@Mock
	private WebCmsDomainRepository domainRepository;

	@InjectMocks
	private WebCmsMultiDomainAdminUiService adminUiService;

	@Before
	public void before() {
		WebCmsDomainContextHolder.clearWebCmsDomainContext();

		when( entityRegistry.getEntityConfiguration( WebCmsDomain.class ) ).thenReturn( entityConfiguration );
	}

	@After
	public void after() {
		WebCmsDomainContextHolder.clearWebCmsDomainContext();
	}

	@Test
	public void selectedDomainIsNullIfNoneSet() {
		assertNull( adminUiService.getSelectedDomain() );
	}

	@Test
	public void selectedDomainIsTheDomainAttachedToTheContext() {
		WebCmsDomainContextHolder.setWebCmsDomainContext( new WebCmsDomainContext( one, null ) );
		assertSame( one, adminUiService.getSelectedDomain() );
	}

	@Test
	public void visibleDomainsIsEmptyIfNoDomainSelectedButNotAllowed() {
		assertEquals( Collections.emptyList(), adminUiService.getVisibleDomains() );
	}

	@Test
	public void visibleDomainsContainsNullIfNoDomainSelectedButNotAllowed() {
		when( multiDomainConfiguration.isNoDomainAllowed() ).thenReturn( true );
		assertEquals( Collections.singletonList( null ), adminUiService.getVisibleDomains() );
	}

	@Test
	public void visibleDomainsIsSameAsSelectedDomainIfNoDomainNotAllowed() {
		when( multiDomainConfiguration.isNoDomainAllowed() ).thenReturn( true );
		WebCmsDomainContextHolder.setWebCmsDomainContext( new WebCmsDomainContext( one, null ) );
		assertEquals( Arrays.asList( one, null ), adminUiService.getVisibleDomains() );
	}

	@Test
	public void noDomainsAreAccessibleIfPermissionsButNoDomain() {
		when( domainRepository.findAll() ).thenReturn( Collections.emptyList() );

		AllowableActions actions = mock( AllowableActions.class );
		when( entityConfiguration.getAllowableActions() ).thenReturn( actions );

		assertEquals( Collections.emptyList(), adminUiService.getAccessibleDomains() );

		verify( actions, never() ).contains( any() );
	}

	@Test
	public void noDomainsAreAccessibleIfNoPermissions() {
		when( multiDomainConfiguration.isNoDomainAllowed() ).thenReturn( true );
		when( domainRepository.findAll() ).thenReturn( Collections.emptyList() );

		AllowableActions actions = mock( AllowableActions.class );
		when( entityConfiguration.getAllowableActions() ).thenReturn( actions );

		assertEquals( Collections.emptyList(), adminUiService.getAccessibleDomains() );

		verify( actions ).contains( AllowableAction.READ );
	}

	@Test
	public void accessibleDomainsContainsNullIfUserAllowedToManageDomainType() {
		when( multiDomainConfiguration.isNoDomainAllowed() ).thenReturn( true );
		when( domainRepository.findAll() ).thenReturn( Collections.emptyList() );

		AllowableActions actions = mock( AllowableActions.class );
		when( entityConfiguration.getAllowableActions() ).thenReturn( actions );

		when( actions.contains( AllowableAction.READ ) ).thenReturn( true );

		assertEquals( Collections.singletonList( null ), adminUiService.getAccessibleDomains() );
	}

	@Test
	public void accessibleNullDomainWithSpecificPermissions() {
		when( multiDomainConfiguration.isNoDomainAllowed() ).thenReturn( true );
		when( domainRepository.findAll() ).thenReturn( Collections.emptyList() );

		AllowableActions actions = mock( AllowableActions.class );
		when( entityConfiguration.getAllowableActions() ).thenReturn( actions );

		when( actions.contains( AllowableAction.CREATE ) ).thenReturn( true );

		assertEquals( Collections.singletonList( null ), adminUiService.getAccessibleDomains( AllowableAction.ADMINISTER, AllowableAction.CREATE ) );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void domainsWithTheRightActionsAreReturned() {
		when( domainRepository.findAll() ).thenReturn( Arrays.asList( one, two ) );

		AllowableActions actionsOne = mock( AllowableActions.class );
		when( entityConfiguration.getAllowableActions( one ) ).thenReturn( actionsOne );
		AllowableActions actionsTwo = mock( AllowableActions.class );
		when( entityConfiguration.getAllowableActions( two ) ).thenReturn( actionsTwo );

		when( actionsOne.contains( AllowableAction.READ ) ).thenReturn( true );
		when( actionsOne.contains( AllowableAction.CREATE ) ).thenReturn( true );
		when( actionsTwo.contains( AllowableAction.READ ) ).thenReturn( true );
		when( actionsTwo.contains( AllowableAction.ADMINISTER ) ).thenReturn( true );

		assertEquals( Arrays.asList( one, two ), adminUiService.getAccessibleDomains() );
		assertEquals( Collections.singletonList( one ), adminUiService.getAccessibleDomains( AllowableAction.CREATE ) );
		assertEquals( Collections.singletonList( two ), adminUiService.getAccessibleDomains( AllowableAction.ADMINISTER ) );
		assertEquals( Arrays.asList( one, two ), adminUiService.getAccessibleDomains( AllowableAction.ADMINISTER, AllowableAction.CREATE ) );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void combinationOfDomainsAndNoDomain() {
		when( multiDomainConfiguration.isNoDomainAllowed() ).thenReturn( true );

		AllowableActions actions = mock( AllowableActions.class );
		when( entityConfiguration.getAllowableActions() ).thenReturn( actions );
		when( actions.contains( AllowableAction.CREATE ) ).thenReturn( true );

		when( domainRepository.findAll() ).thenReturn( Arrays.asList( one, two ) );

		AllowableActions actionsOne = mock( AllowableActions.class );
		when( entityConfiguration.getAllowableActions( one ) ).thenReturn( actionsOne );
		AllowableActions actionsTwo = mock( AllowableActions.class );
		when( entityConfiguration.getAllowableActions( two ) ).thenReturn( actionsTwo );

		when( actionsOne.contains( AllowableAction.READ ) ).thenReturn( true );
		when( actionsOne.contains( AllowableAction.CREATE ) ).thenReturn( true );
		when( actionsTwo.contains( AllowableAction.READ ) ).thenReturn( true );
		when( actionsTwo.contains( AllowableAction.ADMINISTER ) ).thenReturn( true );

		assertEquals( Arrays.asList( one, two ), adminUiService.getAccessibleDomains() );
		assertEquals( Arrays.asList( null, one ), adminUiService.getAccessibleDomains( AllowableAction.CREATE ) );
		assertEquals( Collections.singletonList( two ), adminUiService.getAccessibleDomains( AllowableAction.ADMINISTER ) );
		assertEquals( Arrays.asList( null, one, two ), adminUiService.getAccessibleDomains( AllowableAction.ADMINISTER, AllowableAction.CREATE ) );
	}
}
