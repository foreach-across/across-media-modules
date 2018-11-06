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

package com.foreach.across.modules.filemanager.business.reference;

import com.foreach.across.modules.filemanager.business.reference.properties.FileReferenceProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Event DTO used for modifying a newly created {@link FileReference} as well as configuring additional properties through {@link FileReferenceProperties}.
 *
 * @author Steven Gentens
 * @see FileReferenceService
 * @since 1.3.0
 */
@Getter
@Setter
public class FileReferenceCreationEvent
{
	private FileReference fileReference;
	private FileReferenceProperties fileReferenceProperties;

	public FileReferenceCreationEvent( FileReference fileReference,
	                                   FileReferenceProperties fileReferenceProperties ) {
		this.fileReference = fileReference;
		this.fileReferenceProperties = fileReferenceProperties;
	}
}
