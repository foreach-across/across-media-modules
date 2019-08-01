package com.foreach.across.modules.filemanager.config;

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.modules.filemanager.web.FileReferenceController;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.context.PrefixingPathContext;
import com.foreach.across.modules.web.context.PrefixingPathRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the <code>@fileReference</code> path prefix in Across Web.
 * Allows for generating the file download url: <code>@fileReference:/UUID</code>.
 *
 * @author Steven Gentens
 * @see com.foreach.across.modules.filemanager.utils.FileReferenceUtils
 * @since 1.3.0
 */
@ConditionalOnAcrossModule(allOf = AcrossWebModule.NAME)
@Configuration
class UrlPrefixingConfiguration
{
	private static final String FILE_REFERENCE = "fileReference";

	@Autowired
	public void configureFileReferenceResource( PrefixingPathRegistry prefixingPathRegistry ) {
		PrefixingPathContext resourceContext = new PrefixingPathContext( FileReferenceController.BASE_PATH );
		prefixingPathRegistry.add( FILE_REFERENCE, resourceContext );
	}
}
