/*
 * Copyright 2014 the original author or authors
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

package com.foreach.across.modules.filemanager.config;

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.modules.filemanager.business.FileManagerDomain;
import com.foreach.across.modules.filemanager.business.reference.properties.FileReferencePropertiesServiceImpl;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.hibernate.jpa.repositories.config.EnableAcrossJpaRepositories;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@ConditionalOnAcrossModule(allOf = AcrossHibernateJpaModule.NAME)
@Configuration
@EnableAcrossJpaRepositories(basePackageClasses = FileManagerDomain.class, excludeFilters = {
		// exclude the FileReferencePropertieServiceImpl from repository scanning (impl postfix)
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = FileReferencePropertiesServiceImpl.class)
})
public class DomainConfiguration
{
}
