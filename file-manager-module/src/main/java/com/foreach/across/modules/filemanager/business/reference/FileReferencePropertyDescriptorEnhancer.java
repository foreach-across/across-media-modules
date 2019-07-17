package com.foreach.across.modules.filemanager.business.reference;

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.modules.bootstrapui.elements.FormViewElement;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.entity.registry.properties.*;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Registers a {@link EntityAttributes#FORM_ENCTYPE} attribute if the property is a {@link FileReference} to support {@link org.springframework.web.multipart.MultipartFile}s.
 * Note: when manually adding a {@link FileReference} property, the {@link EntityAttributes#FORM_ENCTYPE} must be manually registered as well.
 *
 * @author Steven Gentens
 * @since 1.3.0
 */
@Component
@ConditionalOnAcrossModule(EntityModule.NAME)
public class FileReferencePropertyDescriptorEnhancer implements DefaultEntityPropertyRegistryProvider.PropertiesRegistrar
{
	@Override
	public final void accept( Class<?> entityType, MutableEntityPropertyRegistry registry ) {
		for ( EntityPropertyDescriptor descriptor : new ArrayList<>( registry.getRegisteredDescriptors() ) ) {
			MutableEntityPropertyDescriptor property = registry.getProperty( descriptor.getName() );

			enhance( registry, property );
		}
	}

	private void enhance( MutableEntityPropertyRegistry registry, MutableEntityPropertyDescriptor descriptor ) {
		TypeDescriptor propertyTypeDescriptor = descriptor.getPropertyTypeDescriptor();

		if ( propertyTypeDescriptor != null ) {
			if ( FileReference.class.isAssignableFrom( propertyTypeDescriptor.getObjectType() ) ) {
				descriptor.setAttribute( EntityAttributes.FORM_ENCTYPE, FormViewElement.ENCTYPE_MULTIPART );
			}
			else if ( propertyTypeDescriptor.isCollection() ) {
				MutableEntityPropertyDescriptor member = registry.getProperty( descriptor.getName() + EntityPropertyRegistry.INDEXER );

				if ( member != null ) {
					enhance( registry, member );
					if ( member.hasAttribute( EntityAttributes.FORM_ENCTYPE ) ) {
						descriptor.setAttribute( EntityAttributes.FORM_ENCTYPE, FormViewElement.ENCTYPE_MULTIPART );
					}
				}
			}
		}
	}
}
