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

package com.foreach.across.modules.webcms.domain.component;

import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import com.foreach.across.modules.webcms.domain.WebCmsChildComponentRestrictable;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifier;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierLink;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierLinkRepository;
import com.querydsl.core.types.Predicate;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.convert.TypeDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static junit.framework.TestCase.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestDefaultAllowedComponentTypeFetcher
{
	private WebCmsDomain domain = WebCmsDomain.NONE;
	@Mock
	private WebCmsDataConversionService dataConversionService;

	@Mock
	private WebCmsComponentTypeRepository componentTypeRepository;

	@Mock
	private WebCmsTypeSpecifierLinkRepository typeLinkRepository;

	@Mock
	private WebCmsMultiDomainService multiDomainService;

	@InjectMocks
	private DefaultAllowedComponentTypeFetcher componentTypeFetcher;

	@Test
	public void supports() {
		when( dataConversionService.convert( any(), eq( TypeDescriptor.valueOf( Boolean.class ) ) ) ).thenReturn( true );
		val attr = new HashMap<String, String>();
		attr.put( WebCmsChildComponentRestrictable.CHILD_COMPONENT_RESTRICTED, "true" );

		WebCmsComponentType componentType = WebCmsComponentType.builder()
		                                                       .attributes( attr )
		                                                       .build();
		WebCmsObject owner = WebCmsComponent.builder()
		                                    .objectId( "wcm:component:my-object-id" )
		                                    .componentType( componentType )
		                                    .build();
		assertTrue( componentTypeFetcher.supports( owner, domain ) );

		WebCmsObject otherOwner = ( (WebCmsComponent) owner ).toBuilder().objectId( "wcm:component:my-other-object-id" )
		                                                     .componentType( new WebCmsComponentType() )
		                                                     .build();
		assertTrue( componentTypeFetcher.supports( otherOwner, domain ) );
		assertTrue( componentTypeFetcher.supports( null, domain ) );
	}

	@Test
	public void fetchComponentTypesByLinks() {
		when( dataConversionService.convert( any(), eq( TypeDescriptor.valueOf( Boolean.class ) ) ) ).thenReturn( true );

		val attr = new HashMap<String, String>();
		attr.put( WebCmsChildComponentRestrictable.CHILD_COMPONENT_RESTRICTED, "true" );

		WebCmsComponentType componentType = WebCmsComponentType.builder()
		                                                       .objectId( "wcm:type:component:my-object-id" )
		                                                       .attributes( attr )
		                                                       .build();
		List<WebCmsTypeSpecifierLink> links = new ArrayList<>();
		links.add( getTypeSpecifierLink( componentType, getComponentType( "one", null, null ) ) );
		links.add( getTypeSpecifierLink( componentType, getComponentType( "three", WebCmsComponentType.COMPONENT_RESTRICTED, "true" ) ) );
		links.add( getTypeSpecifierLink( componentType, getComponentType( "four", WebCmsComponentType.COMPONENT_RESTRICTED, "true" ) ) );
		links.add( getTypeSpecifierLink( componentType, getComponentType( "five", WebCmsChildComponentRestrictable.CHILD_COMPONENT_RESTRICTED, "true" ) ) );

		when( typeLinkRepository.findAllByOwnerObjectIdAndLinkTypeOrderBySortIndexAsc( eq( "wcm:type:component:my-object-id" ),
		                                                                               eq( DefaultAllowedComponentTypeFetcher.ALLOWED_COMPONENT_LINK ) ) )
				.thenReturn( links );

		WebCmsObject owner = WebCmsComponent.builder()
		                                    .objectId( "wcm:component:my-object-id" )
		                                    .componentType( componentType )
		                                    .build();
		List<WebCmsComponentType> types = componentTypeFetcher.fetchComponentTypes( owner, domain );
		assertNotNull( types );
		assertEquals( 4, types.size() );
		assertEquals( 1, types.stream().filter( comp -> "one".equals( comp.getName() ) ).count() );
		assertEquals( 1, types.stream().filter( comp -> "three".equals( comp.getName() ) ).count() );
		assertEquals( 1, types.stream().filter( comp -> "four".equals( comp.getName() ) ).count() );
		assertEquals( 1, types.stream().filter( comp -> "five".equals( comp.getName() ) ).count() );

	}

	@Test
	public void fetchComponentTypes() {
		when( dataConversionService.convert( any(), eq( TypeDescriptor.valueOf( Boolean.class ) ) ) ).thenReturn( true );
		when( multiDomainService.getCurrentDomainForType( WebCmsComponentType.class ) ).thenReturn( WebCmsDomain.NONE );

		val attr = new HashMap<String, String>();
		attr.put( WebCmsChildComponentRestrictable.CHILD_COMPONENT_RESTRICTED, "true" );

		WebCmsComponentType componentType = WebCmsComponentType.builder()
		                                                       .objectId( "wcm:type:component:my-object-id" )
		                                                       .build();
		List<WebCmsComponentType> componentTypes = getComponentTypes();
		when( componentTypeRepository.findAll( Mockito.<Predicate>anyObject() ) ).thenReturn( componentTypes );

		WebCmsObject owner = WebCmsComponent.builder()
		                                    .objectId( "wcm:component:my-object-id" )
		                                    .componentType( componentType )
		                                    .build();
		Collection<WebCmsComponentType> types = componentTypeFetcher.fetchComponentTypes( owner, domain );
		assertNotNull( types );
		assertEquals( 3, types.size() );
		assertEquals( 1, types.stream().filter( comp -> "one".equals( comp.getName() ) ).count() );
		assertEquals( 1, types.stream().filter( comp -> "five".equals( comp.getName() ) ).count() );
		assertEquals( 1, types.stream().filter( comp -> "six".equals( comp.getName() ) ).count() );
		assertEquals( 0, types.stream().filter( comp -> {
			String attribute = comp.getAttribute( WebCmsComponentType.COMPONENT_RESTRICTED );
			return attribute != null && "true".equals( attribute );
		} ).count() );
	}

	private List<WebCmsComponentType> getComponentTypes() {
		List<WebCmsComponentType> links = new ArrayList<>();

		links.add( getComponentType( "one", null, null ) );
		links.add( getComponentType( "two", WebCmsComponentType.COMPONENT_RESTRICTED, "true" ) );
		links.add( getComponentType( "three", WebCmsComponentType.COMPONENT_RESTRICTED, "true" ) );
		links.add( getComponentType( "four", WebCmsComponentType.COMPONENT_RESTRICTED, "true" ) );
		links.add( getComponentType( "five", WebCmsChildComponentRestrictable.CHILD_COMPONENT_RESTRICTED, "true" ) );
		links.add( getComponentType( "six", WebCmsChildComponentRestrictable.CHILD_COMPONENT_RESTRICTED, "true" ) );

		return links;
	}

	private WebCmsTypeSpecifierLink getTypeSpecifierLink( WebCmsObject owner, WebCmsTypeSpecifier typeSpecifier ) {
		WebCmsTypeSpecifierLink link = new WebCmsTypeSpecifierLink();
		link.setOwner( owner );
		link.setTypeSpecifier( typeSpecifier );
		link.setLinkType( DefaultAllowedComponentTypeFetcher.ALLOWED_COMPONENT_LINK );
		return link;
	}

	private WebCmsComponentType getComponentType( String name, String attrKey, String value ) {
		WebCmsComponentType type = WebCmsComponentType.builder()
		                                              .name( name )
		                                              .objectId( "wcm:type:component:" + name )
		                                              .domain( WebCmsDomain.NONE )
		                                              .build();
		if ( !StringUtils.isEmpty( attrKey ) ) {
			type = type.toBuilder().attribute( attrKey, value ).build();
		}
		return type;
	}
}
