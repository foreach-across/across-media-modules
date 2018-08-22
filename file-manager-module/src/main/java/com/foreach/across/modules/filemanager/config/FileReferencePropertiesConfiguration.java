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
import com.foreach.across.modules.filemanager.FileManagerModule;
import com.foreach.across.modules.filemanager.business.reference.properties.*;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.properties.PropertiesModule;
import com.foreach.across.modules.properties.config.AbstractEntityPropertiesConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
@Configuration
@ConditionalOnAcrossModule(allOf = { AcrossHibernateJpaModule.NAME, PropertiesModule.NAME })
public class FileReferencePropertiesConfiguration extends AbstractEntityPropertiesConfiguration
{
	public static final String TABLE_FILE_REFERENCE_PROPERTIES = "fmm_file_ref_props";
	public static final String COLUMN_FILE_REFERENCE_ID = "file_ref_id";
	public static final String ID = FileManagerModule.NAME + ".FileReferenceProperties";

	@Override
	protected String originalTableName() {
		return TABLE_FILE_REFERENCE_PROPERTIES;
	}

	@Override
	public FileReferencePropertiesService service() {
		return new FileReferencePropertiesServiceImpl( registry(), fileReferencePropertiesRepository() );
	}

	@Bean
	public FileReferencePropertiesRepository fileReferencePropertiesRepository() {
		return new FileReferencePropertiesRepository( this );
	}

	@Override
	public Class<?> entityClass() {
		return FileReferenceProperties.class;
	}

	@Override
	public String propertiesId() {
		return ID;
	}

	@Override
	public String keyColumnName() {
		return COLUMN_FILE_REFERENCE_ID;
	}

	@Override
	public FileReferencePropertiesRegistry registry() {
		return new FileReferencePropertiesRegistry( this );
	}
}
