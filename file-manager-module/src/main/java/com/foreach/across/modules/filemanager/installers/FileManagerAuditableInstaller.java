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

package com.foreach.across.modules.filemanager.installers;

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.core.annotations.Installer;
import com.foreach.across.modules.filemanager.business.reference.FileReference;
import com.foreach.across.modules.hibernate.installers.AuditableSchemaInstaller;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.annotation.Order;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
@ConditionalOnAcrossModule(allOf = AcrossHibernateJpaModule.NAME)
@ConditionalOnClass(AuditableSchemaInstaller.class)
@Order(3)
@Installer(description = "Adds auditing columns to core tables", version = 1)
public class FileManagerAuditableInstaller extends AuditableSchemaInstaller
{
	@Override
	protected Collection<String> getTableNames() {
		return Collections.singletonList( FileReference.TABLE_FILE_REFERENCE );
	}
}
