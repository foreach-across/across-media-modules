package com.foreach.across.modules.filemanager.views.bootstrapui;

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.modules.bootstrapui.elements.BootstrapUiBuilders;
import com.foreach.across.modules.bootstrapui.elements.builder.LinkViewElementBuilder;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.entity.conditionals.ConditionalOnBootstrapUI;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import com.foreach.across.modules.filemanager.business.reference.FileReference;
import com.foreach.across.modules.filemanager.utils.FileReferenceUtils;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.web.ui.MutableViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElementBuilderSupport;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
@ConditionalOnBootstrapUI
@ConditionalOnAcrossModule(allOf = { AcrossHibernateJpaModule.NAME, EntityModule.NAME })
public class FileReferenceValueViewElementBuilder extends ViewElementBuilderSupport
{
	@Override
	protected MutableViewElement createElement( ViewElementBuilderContext builderContext ) {
		FileReference fileReference = EntityViewElementUtils.currentPropertyValue( builderContext, FileReference.class );
		LinkViewElementBuilder link = BootstrapUiBuilders.link();
		if ( fileReference != null ) {
			link.text( fileReference.getName() )
			    .url( builderContext.buildLink( FileReferenceUtils.getDownloadUrl( fileReference ) ) );
		}
		return link.build( builderContext );
	}

}
