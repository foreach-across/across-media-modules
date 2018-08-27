package com.foreach.across.modules.filemanager.views.bootstrapui;

import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.ViewElementTypeLookupStrategy;
import com.foreach.across.modules.filemanager.business.reference.FileReference;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Registers a {@link FileReferenceViewElementBuilder} for {@link FileReference} properties.
 *
 * @author Steven Gentens
 * @see FileReferenceViewElementBuilderFactory
 * @since 1.3.0
 */
@Component
@Order(1)
public class FileReferenceViewElementLookupStrategy implements ViewElementTypeLookupStrategy
{
	@Override
	public String findElementType( EntityPropertyDescriptor entityPropertyDescriptor, ViewElementMode viewElementMode ) {
		Class<?> propertyType = entityPropertyDescriptor.getPropertyType();
		if ( propertyType != null && FileReference.class.isAssignableFrom( propertyType ) ) {
			if ( !viewElementMode.isForMultiple() && ViewElementMode.CONTROL.equals( viewElementMode ) ) {
				return FileReferenceViewElementBuilderFactory.FILE_REFERENCE_CONTROL;
			}
		}
		return null;
	}
}
