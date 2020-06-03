package com.foreach.across.modules.filemanager.views.bootstrapui;

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.modules.bootstrapui.BootstrapUiModule;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.ViewElementTypeLookupStrategy;
import com.foreach.across.modules.filemanager.business.reference.FileReference;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;

/**
 * Registers a {@link FileReferenceControlViewElementBuilder} for {@link FileReference} properties.
 *
 * @author Steven Gentens
 * @see FileReferenceViewElementBuilderFactory
 * @since 1.3.0
 */
@Component
@Order(1)
@ConditionalOnAcrossModule(allOf = { BootstrapUiModule.NAME, AcrossHibernateJpaModule.NAME, EntityModule.NAME })
public class FileReferenceViewElementLookupStrategy implements ViewElementTypeLookupStrategy
{
	@Override
	public String findElementType( EntityPropertyDescriptor descriptor, ViewElementMode viewElementMode ) {
		TypeDescriptor propertyType = descriptor.getPropertyTypeDescriptor();

		if ( propertyType == null ) {
			return null;
		}

		Class type = propertyType.isCollection() ? propertyType.getElementTypeDescriptor().getType() : propertyType.getType();
		if ( type != null
				&& FileReference.class.isAssignableFrom( type )
				&& ( ViewElementMode.isValue( viewElementMode ) || isNonFilterControl( viewElementMode ) ) ) {
			return FileReferenceViewElementBuilderFactory.FILE_REFERENCE_CONTROL;
		}

		return null;
	}

	private boolean isNonFilterControl( ViewElementMode viewElementMode ) {
		ViewElementMode single = viewElementMode.forSingle();
		return ViewElementMode.CONTROL.equals( single ) || ViewElementMode.LIST_CONTROL.equals( single );
	}
}
