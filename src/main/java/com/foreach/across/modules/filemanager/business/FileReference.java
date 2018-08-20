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

package com.foreach.across.modules.filemanager.business;

import com.foreach.across.modules.hibernate.business.AuditableEntity;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Persistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = FileReference.TABLE_FILE_REFERENCE)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FileReference extends AuditableEntity implements Persistable<String>
{
	public static final String TABLE_FILE_REFERENCE = "fmm_file_reference";

	@Id
	private String id;

	@NonNull
	@Column(name = "uuid")
	private String uuid;

	@NonNull
	@Column(name = "name")
	private String name;

	@NonNull
	@Column(name = "file_descriptor")
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private String fileDescriptor;

	@Column(name = "file_size")
	private Long fileSize;

	@Column(name = "mime_type")
	private String mimeType;

	@Column(name = "hash")
	private String hash;

	private FileDescriptor getFileDescriptor() {
		return new FileDescriptor( fileDescriptor );
	}

	private void setFileDescriptor( FileDescriptor fileDescriptor ) {
		this.fileDescriptor = fileDescriptor.getUri();
	}

	@Override
	public boolean isNew() {
		return StringUtils.isBlank( id );
	}
}
