package com.foreach.across.modules.filemanager.business.reference;

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.properties.PropertiesModule;
import com.foreach.across.modules.web.AcrossWebModule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Responsible for converting {@link MultipartFile} instances to a {@link FileReference} and uploading the
 * physical file to a {@link com.foreach.across.modules.filemanager.services.FileRepository} with the specified {@link #getRepositoryId()}.
 * <p/>
 * If no {@link #repositoryId} is specified, the {@link FileManager#DEFAULT_REPOSITORY} will be used.
 *
 * @author Steven Gentens
 * @since 1.3.0
 */
@RequiredArgsConstructor
@Component
@Exposed
@ConditionalOnAcrossModule(allOf = { EntityModule.NAME, AcrossHibernateJpaModule.NAME, PropertiesModule.NAME })
public class MultipartFileToFileReferenceConverter implements Converter<MultipartFile, FileReference>
{
	private final FileReferenceService fileReferenceService;

	/**
	 * The id of the repository to which the files should be uploaded.
	 */
	@Getter
	@Setter
	private String repositoryId = FileManager.DEFAULT_REPOSITORY;

	@Override
	public FileReference convert( MultipartFile source ) {
		return source.isEmpty() ? null : fileReferenceService.save( source, repositoryId );
	}

	@Autowired
	void registerToMvcConversionService( @Qualifier(AcrossWebModule.CONVERSION_SERVICE_BEAN) ConverterRegistry mvcConversionService ) {
		mvcConversionService.addConverter( this );
	}

}
