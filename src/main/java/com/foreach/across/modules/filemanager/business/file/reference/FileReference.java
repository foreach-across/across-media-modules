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

package com.foreach.across.modules.filemanager.business.file.reference;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.hibernate.business.AuditableEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.domain.Persistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents the reference to a stored file.
 *
 * @author Steven Gentens
 * @since 1.3.0
 */
@Entity
@Table(name = FileReference.TABLE_FILE_REFERENCE)
@Getter
@Setter
public class FileReference extends AuditableEntity implements Persistable<String>
{
	public static final String TABLE_FILE_REFERENCE = "fmm_file_reference";

	@Id
	@Setter(AccessLevel.NONE)
	private String id;

	@Size(max = 255)
	@NotBlank
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

	public FileReference() {
		setId( "" );
	}

	public FileReference( String id, String name, FileDescriptor fileDescriptor, Long fileSize, String mimeType, String hash ) {
		this.id = id;
		this.name = name;
		setFileDescriptor( fileDescriptor );
		setId( id );
		this.fileSize = fileSize;
		this.mimeType = mimeType;
		this.hash = hash;
	}

	public void setId( String id ) {
		this.id = StringUtils.isNotEmpty( id ) ? id : UUID.randomUUID().toString();
	}

	public FileDescriptor getFileDescriptor() {
		return new FileDescriptor( fileDescriptor );
	}

	public void setFileDescriptor( FileDescriptor fileDescriptor ) {
		this.fileDescriptor = fileDescriptor.getUri();
	}

	@Override
	public boolean isNew() {
		return StringUtils.isBlank( id );
	}

	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		else if ( o != null && getClass().isAssignableFrom( o.getClass() ) ) {
			FileReference that = (FileReference) o;
			if ( isNew() ) {
				return this == that;
			}
			else {
				return Objects.equals( getId(), that.getId() );
			}
		}
		else {
			return false;
		}
	}

	public int hashCode() {
		return isNew() ? super.hashCode() : Objects.hash( new Object[] { getId() } );
	}

}
