package com.foreach.imageserver.test.standalone.module.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.services.ImageContextService;
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
	private ImageContextService contextService;

	@Autowired
	private ImageService imageService;

	@InstallerMethod
	public void install() {
		createContext( "website" );
		createContext( "tablet" );

		createResolution( 640, 480, false, Arrays.asList( "low-res" ), Arrays.asList( "website" ) );
		createResolution( 320, 240, false, Arrays.asList( "low-res-d2" ), Arrays.asList( "website" ) );
		createResolution( 160, 120, false, Arrays.asList( "low-res-d4" ), Arrays.asList( "website" ) );
		createResolution( 800, 600, true, Arrays.asList( "" ), Arrays.asList( "tablet" ) );
		createResolution( 400, 300, true, Arrays.asList( "" ), Arrays.asList( "tablet" ) );
		createResolution( 1600, 1200, true, Arrays.asList( "" ), Arrays.asList( "tablet" ) );
		createResolution( 1024, 768, true, Arrays.asList( "high-res", "maximum" ), Arrays.asList( "website", "tablet" ) );
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
			resolution.setAllowedOutputTypes( EnumSet.of( ImageType.JPEG, ImageType.PNG, ImageType.GIF ) );
			resolution.setPregenerateVariants( true );

			List<ImageContext> contexts = new ArrayList<>( contextCodes.size() );

			for ( String code : contextCodes ) {
				contexts.add( contextService.getByCode( code ) );
			}

			resolution.setContexts( contexts );

			imageService.saveImageResolution( resolution );
		}
	}

	private void createContext( String code ) {
		ImageContext existing = contextService.getByCode( code );

		if ( existing == null ) {
			ImageContextDto context = new ImageContextDto();
			context.setCode( code );

			contextService.save( context );
		}
	}
}
