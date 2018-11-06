package com.foreach.across.modules.filemanager.business.reference;

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.modules.filemanager.business.reference.properties.FileReferencePropertiesService;
import com.foreach.across.modules.hibernate.aop.EntityInterceptorAdapter;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.properties.PropertiesModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
@RequiredArgsConstructor
@ConditionalOnAcrossModule(allOf = { AcrossHibernateJpaModule.NAME, PropertiesModule.NAME })
@Component
public class FileReferenceInterceptor extends EntityInterceptorAdapter<FileReference>
{
	private final FileReferencePropertiesService fileReferencePropertiesService;

	@Override
	public boolean handles( Class<?> aClass ) {
		return FileReference.class.isAssignableFrom( aClass );
	}

	@Override
	public void beforeDelete( FileReference entity ) {
		fileReferencePropertiesService.deleteProperties( entity.getId() );
	}
}
