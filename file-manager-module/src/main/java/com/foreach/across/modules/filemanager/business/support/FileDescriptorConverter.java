package com.foreach.across.modules.filemanager.business.support;

import com.foreach.across.modules.filemanager.business.FileDescriptor;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * JPA converter for {@link FileDescriptor} type.
 *
 * @author Arne Vandamme
 * @since 1.3.0
 */
@SuppressWarnings("unused")
@Converter(autoApply = true)
public final class FileDescriptorConverter implements AttributeConverter<FileDescriptor, String>
{
	@Override
	public String convertToDatabaseColumn( FileDescriptor attribute ) {
		return attribute != null ? attribute.getUri() : null;
	}

	@Override
	public FileDescriptor convertToEntityAttribute( String dbData ) {
		return dbData != null ? FileDescriptor.of( dbData ) : null;
	}
}
