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

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.hibernate.business.SettableIdAuditableEntity;
import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
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
@SuppressWarnings("squid:S2160")
public class FileReference extends SettableIdAuditableEntity<FileReference>
{
	public static final String TABLE_FILE_REFERENCE = "fmm_file_reference";

	@Id
	@GeneratedValue(generator = "seq_fmm_file_ref_id")
	@GenericGenerator(
			name = "seq_fmm_file_ref_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@Parameter(name = "sequenceName", value = "seq_fmm_file_ref_id"),
					@Parameter(name = "allocationSize", value = "1")
			}
	)
	private Long id;

	@Length(max = 255)
	@NotBlank
	@Column(name = "uuid")
	@Pattern(regexp = "^\\p{ASCII}*$")
	private String uuid;

	@Length(max = 255)
	@NotBlank
	@Column(name = "name")
	private String name;

	@NotNull
	@Column(name = "file_descriptor")
	private FileDescriptor fileDescriptor;

	@Column(name = "file_size")
	private Long fileSize;

	@Column(name = "mime_type")
	private String mimeType;

	@Length(max = 255)
	@Column(name = "hash")
	private String hash;

	@SuppressWarnings( "squid:S2637" )
	public FileReference() {
		setUuid( "" );
	}

	@Builder(toBuilder = true)
	public FileReference( Long id, String uuid, String name, FileDescriptor fileDescriptor, Long fileSize, String mimeType, String hash ) {
		this.id = id;
		this.name = name;
		this.fileDescriptor = fileDescriptor;
		setUuid( uuid );
		this.fileSize = fileSize;
		this.mimeType = mimeType;
		this.hash = hash;
	}

	public void setUuid( String uuid ) {
		this.uuid = StringUtils.isNotEmpty( uuid ) ? uuid : UUID.randomUUID().toString();
	}

}
