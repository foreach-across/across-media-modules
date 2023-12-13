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

package com.foreach.across.modules.webcms.domain;

import com.foreach.across.modules.hibernate.aop.EntityInterceptorAdapter;
import com.foreach.across.modules.webcms.domain.component.QWebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentRepository;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Removes components when a {@link WebCmsObject} is being deleted.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
@Slf4j
class WebCmsObjectDeleteInterceptor extends EntityInterceptorAdapter<WebCmsObject>
{
	private final WebCmsComponentRepository componentRepository;
	private final WebCmsTypeSpecifierLinkRepository linkRepository;

	@Override
	public boolean handles( Class<?> entityClass ) {
		return WebCmsObject.class.isAssignableFrom( entityClass );
	}

	/**
	 * After the initial entity has been deleted, delete all components linking to this one.
	 * A component can be deleted if no other (proxy) component is pointing to it.
	 *
	 * @param entity component owner
	 */
	@Override
	public void afterDelete( WebCmsObject entity ) {
		deleteOwnedComponents( entity );
		deleteTypeSpecifierLinks( entity );
	}

	private void deleteOwnedComponents( WebCmsObject entity ) {
		componentRepository
				.findAll( QWebCmsComponent.webCmsComponent.ownerObjectId.eq( entity.getObjectId() ) )
				.forEach( component -> {
					// todo: check if there is a proxy pointing to us and if so simply transfer ownership to the first one
					// if not, clear to delete
					LOG.trace( "Deleting component {} because owner {} has been deleted", component, entity );
					componentRepository.delete( component );
				} );
	}

	private void deleteTypeSpecifierLinks( WebCmsObject entity ) {
		linkRepository.findAllByOwnerObjectId( entity.getObjectId() )
		              .forEach( linkRepository::delete );
	}
}
