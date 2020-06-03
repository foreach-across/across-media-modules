package com.foreach.across.modules.filemanager.views.bootstrapui;

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.modules.bootstrapui.BootstrapUiModule;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.views.EntityViewElementBuilderFactory;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Creates a {@link ViewElementBuilder} for {@link FileReferenceViewElementBuilderFactory#FILE_REFERENCE_CONTROL} in single mode.
 *
 * @author Steven Gentens
 * @since 1.3.0
 */
@Component
@ConditionalOnAcrossModule(allOf = { BootstrapUiModule.NAME, AcrossHibernateJpaModule.NAME, EntityModule.NAME })
public class FileReferenceViewElementBuilderFactory implements EntityViewElementBuilderFactory
{
	public static final String FILE_REFERENCE_CONTROL = FileReferenceViewElementBuilderFactory.class.getName() + ".fileReferenceControl";

	@Override
	public boolean supports( String viewElementType ) {
		return StringUtils.equals( FILE_REFERENCE_CONTROL, viewElementType );
	}

	@Override
	@SuppressWarnings("unchecked")
	public ViewElementBuilder createBuilder( EntityPropertyDescriptor entityPropertyDescriptor, ViewElementMode viewElementMode, String viewElementType ) {
		ViewElementMode single = viewElementMode.forSingle();
		if ( ViewElementMode.CONTROL.equals( single ) || ViewElementMode.FORM_WRITE.equals( single ) ) {
			return new FileReferenceControlViewElementBuilder().postProcessor( EntityViewElementUtils.controlNamePostProcessor( entityPropertyDescriptor ) );
		}
		return new FileReferenceValueViewElementBuilder( viewElementMode );
	}

}
