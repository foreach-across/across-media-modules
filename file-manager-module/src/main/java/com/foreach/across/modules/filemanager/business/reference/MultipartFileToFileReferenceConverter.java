package com.foreach.across.modules.filemanager.business.reference;

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.web.AcrossWebModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
@RequiredArgsConstructor
@Component
@ConditionalOnAcrossModule(allOf = { EntityModule.NAME, AcrossHibernateJpaModule.NAME })
public class MultipartFileToFileReferenceConverter implements Converter<MultipartFile, FileReference>
{
	private final FileReferenceService fileReferenceService;

	@Override
	public FileReference convert( MultipartFile source ) {
		return source.isEmpty() ? null : fileReferenceService.save( source );
	}

	@Autowired
	void registerToMvcConversionService( @Qualifier(AcrossWebModule.CONVERSION_SERVICE_BEAN) ConverterRegistry mvcConversionService ) {
		mvcConversionService.addConverter( this );
	}

}
