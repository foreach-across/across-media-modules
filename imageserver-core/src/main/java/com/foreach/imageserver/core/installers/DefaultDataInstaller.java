package com.foreach.imageserver.core.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.imageserver.core.services.ImageProfileService;
import com.foreach.imageserver.dto.ImageProfileDto;
import org.springframework.beans.factory.annotation.Autowired;

@Installer(description = "Installs the default image_profile", version = 1,
           phase = InstallerPhase.AfterModuleBootstrap)
public class DefaultDataInstaller
{
	@Autowired
	private ImageProfileService imageProfileService;

	@InstallerMethod
	public void install() {
		createImageProfile();
	}

	private void createImageProfile() {
		ImageProfileDto imageProfileDto = new ImageProfileDto();
		imageProfileDto.setName( "default" );
		imageProfileService.save( imageProfileDto );
	}

}
