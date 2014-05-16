package com.foreach.imageserver.core.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.across.core.installers.InstallerRunCondition;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.services.ImageService;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import java.util.Date;

@Installer(description = "Installs the 404 dummy image", phase = InstallerPhase.AfterContextBootstrap, version = 1, runCondition = InstallerRunCondition.AlwaysRun)
public class Image404Installer {
    private static final Logger LOG = LoggerFactory.getLogger(Image404Installer.class);

    @Autowired
    private Environment environment;

    @Autowired
    private ImageService imageService;

    @InstallerMethod
    public void setupImage() throws Exception {
        String fallbackImageKey = environment.getProperty("image.404.fallback", "");

        if (StringUtils.isNotBlank(fallbackImageKey)) {

            Image image = imageService.getByExternalId(fallbackImageKey);

            if (image == null) {
                LOG.info("Installing default 404 image under key {}", fallbackImageKey);

                byte[] img = IOUtils.toByteArray(new ClassPathResource("/images/404-1280x960.jpg").getInputStream());
                imageService.saveImage(fallbackImageKey, img, new Date());
            }
        }
    }
}
