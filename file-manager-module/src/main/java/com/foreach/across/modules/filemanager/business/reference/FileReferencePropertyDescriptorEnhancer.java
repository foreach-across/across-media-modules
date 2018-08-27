package com.foreach.across.modules.filemanager.business.reference;

import com.foreach.across.modules.bootstrapui.elements.FormViewElement;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.registry.properties.MutableEntityPropertyDescriptor;
import com.foreach.across.modules.entity.registry.properties.registrars.AbstractEntityPropertyDescriptorEnhancer;
import org.springframework.stereotype.Component;

/**
 * Registers a {@link EntityAttributes#FORM_ENCTYPE} attribute if the property is a {@link FileReference} to support {@link org.springframework.web.multipart.MultipartFile}s.
 * Note: when manually adding a {@link FileReference} property, the {@link EntityAttributes#FORM_ENCTYPE} must be manually registered as well.
 *
 * @author Steven Gentens
 * @since 1.3.0
 */
@Component
public class FileReferencePropertyDescriptorEnhancer extends AbstractEntityPropertyDescriptorEnhancer
{
	@Override
	protected void enhance( Class<?> entityType, MutableEntityPropertyDescriptor descriptor ) {
		Class<?> propertyType = descriptor.getPropertyType();
		if ( propertyType != null && FileReference.class.isAssignableFrom( propertyType ) ) {
			descriptor.setAttribute( EntityAttributes.FORM_ENCTYPE, FormViewElement.ENCTYPE_MULTIPART );
		}
	}
}
