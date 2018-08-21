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
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.properties.PropertiesModule;
import com.foreach.across.modules.properties.installers.EntityPropertiesInstaller;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;

import static com.foreach.across.modules.filemanager.config.FileReferencePropertiesConfiguration.COLUMN_FILE_REFERENCE_ID;
import static com.foreach.across.modules.filemanager.config.FileReferencePropertiesConfiguration.TABLE_FILE_REFERENCE_PROPERTIES;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
@ConditionalOnAcrossModule(allOf = { AcrossHibernateJpaModule.NAME, PropertiesModule.NAME })
@Installer(description = "Installs the file reference properties table", version = 1)
@RequiredArgsConstructor
@Order(1)
public class FileReferencePropertiesSchemaInstaller extends EntityPropertiesInstaller
{
	@Override
	protected String getTableName() {
		return TABLE_FILE_REFERENCE_PROPERTIES;
	}

	@Override
	protected String getKeyColumnName() {
		return COLUMN_FILE_REFERENCE_ID;
	}
}
