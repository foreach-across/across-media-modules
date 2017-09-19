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

import com.foreach.across.modules.webcms.domain.domain.config.WebCmsMultiDomainConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link WebCmsDomainMetadataFactory} that uses the
 * {@link org.springframework.beans.factory.config.AutowireCapableBeanFactory}
 * to build metadata objects as prototype beans (except for the no-domain where the bean is retrieved by name).
 * <p/>
 * The configuration of metadata types and no-domain metadata is read from the {@link WebCmsMultiDomainConfiguration}.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
@Service
@RequiredArgsConstructor
public class WebCmsDomainMetadataFactoryImpl implements WebCmsDomainMetadataFactory
{
	private final WebCmsMultiDomainConfiguration multiDomainConfiguration;
	private final AutowireCapableBeanFactory beanFactory;

	@Override
	public Object createMetadataForDomain( WebCmsDomain domain ) {
		if ( domain == null ) {
			String noDomainMetadataBeanName = multiDomainConfiguration.getNoDomainMetadataBeanName();
			return noDomainMetadataBeanName != null ? beanFactory.getBean( noDomainMetadataBeanName ) : null;
		}

		Class<? extends WebCmsDomainAware> metadataClass = multiDomainConfiguration.getMetadataClass();
		if ( metadataClass != null ) {
			WebCmsDomainAware metadata = beanFactory.createBean( metadataClass );
			metadata.setWebCmsDomain( domain );
			return metadata;
		}

		return null;
	}
}
