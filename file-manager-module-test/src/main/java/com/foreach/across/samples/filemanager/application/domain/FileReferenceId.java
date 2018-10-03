package com.foreach.across.samples.filemanager.application.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class FileReferenceId
{
	@Column(name = "file_reference_id")
	private Long fileReferenceId;
}
