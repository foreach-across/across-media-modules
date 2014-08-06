package com.foreach.imageserver.test.standalone.module.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.imageserver.core.business.Context;
import com.foreach.imageserver.core.services.ContextService;
import com.foreach.imageserver.dto.ImageContextDto;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Arne Vandamme
 */
@Installer(description = "Installs the test contexts", version = 1,
           phase = InstallerPhase.AfterModuleBootstrap)
public class TestsContextsInstaller
{
	@Autowired
	private ContextService contextService;

	@InstallerMethod
	public void install() {
		createContext( "website" );
		createContext( "tablet" );
	}

	private void createContext( String code ) {
		Context existing = contextService.getByCode( code );

		if ( existing == null ) {
			ImageContextDto context = new ImageContextDto();
			context.setCode( code );

			contextService.save( context );
		}
	}
}
