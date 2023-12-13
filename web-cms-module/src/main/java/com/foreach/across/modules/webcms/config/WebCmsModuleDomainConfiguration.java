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

package com.foreach.across.modules.webcms.config;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.context.support.AcrossModuleMessageSource;
import com.foreach.across.modules.hibernate.jpa.repositories.config.EnableAcrossJpaRepositories;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticleType;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.page.WebCmsPageType;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublicationType;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Configuration
@EnableAcrossJpaRepositories(basePackageClasses = WebCmsObject.class)
public class WebCmsModuleDomainConfiguration
{
	@Autowired
	public void registerDefaultTypes( WebCmsTypeRegistry typeRegistry ) {
		typeRegistry.register( WebCmsComponentType.OBJECT_TYPE, WebCmsComponentType.class, WebCmsComponentType::new );
		typeRegistry.register( WebCmsArticleType.OBJECT_TYPE, WebCmsArticleType.class, WebCmsArticleType::new );
		typeRegistry.register( WebCmsPublicationType.OBJECT_TYPE, WebCmsPublicationType.class, WebCmsPublicationType::new );
		typeRegistry.register( WebCmsPageType.OBJECT_TYPE, WebCmsPageType.class, WebCmsPageType::new );
	}

	@Bean(name = "entityValidator")
	@Exposed
	@ConditionalOnMissingBean(name = "entityValidator", search = SearchStrategy.ANCESTORS)
	public SmartValidator entityValidator() {
		LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
		localValidatorFactoryBean.setValidationMessageSource( messageSource() );
		return localValidatorFactoryBean;
	}

	@Bean
	@ConditionalOnMissingBean(name = "entityValidator", search = SearchStrategy.ANCESTORS)
	public MessageSource messageSource() {
		AcrossModuleMessageSource messageSource = new AcrossModuleMessageSource();
		messageSource.setBasenames(
				"classpath:/org/hibernate/validator/ValidationMessages",
				"classpath:/messages/WebCmsModule/default"
		);
		return messageSource;
	}
}
