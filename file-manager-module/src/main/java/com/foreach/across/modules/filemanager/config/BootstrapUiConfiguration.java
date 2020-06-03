package com.foreach.across.modules.filemanager.config;

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.modules.bootstrapui.BootstrapUiModule;
import com.foreach.across.modules.filemanager.FileManagerModuleIcons;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
@Configuration
@ConditionalOnAcrossModule(BootstrapUiModule.NAME)
class BootstrapUiConfiguration
{
	@PostConstruct
	void registerIconSet() {
		FileManagerModuleIcons.registerIconSet();
	}
}
