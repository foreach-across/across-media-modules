package com.foreach.imageserver.core.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.imageserver.core.business.ImageProfile;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.services.ImageContextService;
import com.foreach.imageserver.core.services.ImageProfileService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.dto.ImageContextDto;
import com.foreach.imageserver.dto.ImageProfileDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.EnumSet;

@Installer(description = "Installs the default image_profile", version = 1,
           phase = InstallerPhase.AfterModuleBootstrap)
public class DefaultDataInstaller
{
	@Autowired
	private ImageProfileService imageProfileService;

	@Autowired
	private ImageContextService imageContextService;

	@Autowired
	private ImageService imageService;

	@InstallerMethod
	public void install() {
		createImageProfile();
		createImageContext();
		createDefaultOriginalResolution();
	}

	private void createImageContext() {
		ImageContextDto imageContextDto = new ImageContextDto();
		imageContextDto.setCode( "default" );
		imageContextService.save( imageContextDto );
	}

	private void createImageProfile() {
		ImageProfile profile = imageProfileService.getById( ImageProfile.DEFAULT_PROFILE_ID );

		if ( profile == null ) {
			ImageProfileDto imageProfileDto = new ImageProfileDto();
			imageProfileDto.setNewEntity( true );
			imageProfileDto.setId( ImageProfile.DEFAULT_PROFILE_ID );
			imageProfileDto.setName( "default" );

			imageProfileService.save( imageProfileDto );
		}
	}

	private void createDefaultOriginalResolution() {
		ImageResolution originalResolution = new ImageResolution();
		originalResolution.setWidth( 0 );
		originalResolution.setHeight( 0 );
		originalResolution.setContexts( Collections.singleton( imageContextService.getByCode( "default" ) ) );
		originalResolution.setAllowedOutputTypes( EnumSet.of( ImageType.JPEG, ImageType.PNG, ImageType.GIF ) );

		imageService.saveImageResolution( originalResolution );
	}

}
