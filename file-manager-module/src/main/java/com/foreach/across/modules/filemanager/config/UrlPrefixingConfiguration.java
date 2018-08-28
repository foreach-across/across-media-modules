package com.foreach.across.modules.filemanager.config;

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.modules.filemanager.web.FileReferenceController;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.context.PrefixingPathContext;
import com.foreach.across.modules.web.context.PrefixingPathRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
@ConditionalOnAcrossModule(allOf = AcrossWebModule.NAME)
@Configuration
public class UrlPrefixingConfiguration
{
	public static final String FILE_REFERENCE = "fileReference";

	@Bean
	public Void configureFileReferenceResource( PrefixingPathRegistry prefixingPathRegistry ) {
		PrefixingPathContext resourceContext = new PrefixingPathContext( FileReferenceController.BASE_PATH );
		prefixingPathRegistry.add( FILE_REFERENCE, resourceContext );
		return null;
	}
}
