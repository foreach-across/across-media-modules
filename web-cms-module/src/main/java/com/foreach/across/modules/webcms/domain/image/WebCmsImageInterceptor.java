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

package com.foreach.across.modules.webcms.domain.image;

import com.foreach.across.modules.hibernate.aop.EntityInterceptorAdapter;
import com.foreach.across.modules.webcms.domain.image.connector.WebCmsImageConnector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Takes care of deleting the image data once the transaction that deletes the
 * {@link WebCmsImage} has been committed.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Slf4j
@Component
@RequiredArgsConstructor
class WebCmsImageInterceptor extends EntityInterceptorAdapter<WebCmsImage>
{
	private final WebCmsImageConnector imageConnector;

	@Override
	public boolean handles( Class<?> entityClass ) {
		return WebCmsImage.class.isAssignableFrom( entityClass );
	}

	@Override
	public void afterDelete( WebCmsImage image ) {
		TransactionSynchronizationManager.registerSynchronization(
				new TransactionSynchronizationAdapter()
				{
					@Override
					public void afterCommit() {
						LOG.trace( "Deleting physical image data for {}", image );
						imageConnector.deleteImageData( image );
					}
				}
		);
	}
}
