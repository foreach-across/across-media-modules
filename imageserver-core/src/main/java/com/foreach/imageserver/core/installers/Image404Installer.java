package com.foreach.imageserver.core.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.across.core.installers.InstallerRunCondition;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.services.ImageService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.Date;

@Installer(description = "Installs the 404 dummy image", phase = InstallerPhase.AfterContextBootstrap, version = 1,
		runCondition = InstallerRunCondition.AlwaysRun)
@RequiredArgsConstructor
public class Image404Installer
{
	private static final Logger LOG = LoggerFactory.getLogger( Image404Installer.class );

	private final Environment environment;
	private final ImageService imageService;

	@Value("classpath:/images/404-1280x960gge.jpg")
	private Resource resource;

	@InstallerMethod
	public void setupImage() throws Exception {
		String fallbackImageKey = environment.getProperty( "image.404.fallback", "" );

		if ( StringUtils.isNotBlank( fallbackImageKey ) ) {

			Image image = imageService.getByExternalId( fallbackImageKey );

			if ( image == null ) {
				LOG.info( "Installing default 404 image under key {}", fallbackImageKey );

				try (InputStream is = resource.getInputStream()) {
					imageService.saveImage( fallbackImageKey, IOUtils.toByteArray( is ), new Date(), false );
				}
			}
		}
	}
}
