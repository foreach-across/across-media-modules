package com.foreach.imageserver.test.standalone.module.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.imageserver.core.business.Context;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.services.ContextService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.dto.ImageContextDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * @author Arne Vandamme
 */
@Installer(description = "Installs the test contexts and resolutions", version = 1,
           phase = InstallerPhase.AfterModuleBootstrap)
public class TestContextAndResolutionInstaller
{
	@Autowired
	private ContextService contextService;

	@Autowired
	private ImageService imageService;

	@InstallerMethod
	public void install() {
		createContext( "website" );
		createContext( "tablet" );

		createResolution( 640, 480, false, Arrays.asList( "low-res" ), Arrays.asList( "website" ) );
		createResolution( 800, 600, true, Arrays.asList( "" ), Arrays.asList( "tablet" ) );
		createResolution( 1024, 768, true, Arrays.asList( "high-res", "maximum" ),
		                  Arrays.asList( "website", "tablet" ) );
	}

	private void createResolution( int width,
	                               int height,
	                               boolean configurable,
	                               Collection<String> tags,
	                               Collection<String> contextCodes ) {
		ImageResolution existing = imageService.getResolution( width, height );

		if ( existing == null ) {
			ImageResolution resolution = new ImageResolution();
			resolution.setWidth( width );
			resolution.setHeight( height );
			resolution.setTags( new HashSet<>( tags ) );
			resolution.setConfigurable( configurable );

			List<Context> contexts = new ArrayList<>( contextCodes.size() );

			for ( String code : contextCodes ) {
				contexts.add( contextService.getByCode( code ) );
			}

			resolution.setContexts( contexts );

			imageService.saveImageResolution( resolution, contexts );
		}
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
