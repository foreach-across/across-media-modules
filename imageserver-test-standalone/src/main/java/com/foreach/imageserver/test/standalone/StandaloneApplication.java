package com.foreach.imageserver.test.standalone;

import com.foreach.across.config.AcrossApplication;
import com.foreach.across.modules.debugweb.DebugWebModule;
import com.foreach.across.modules.user.UserModule;
import com.foreach.imageserver.admin.ImageServerAdminWebModule;
import com.foreach.imageserver.admin.ImageServerAdminWebModuleSettings;
import com.foreach.imageserver.core.ImageServerCoreModule;
import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.io.File;
import java.util.UUID;

@AcrossApplication(
        modules = {
                DebugWebModule.NAME,
                UserModule.NAME
        }
)
@PropertySource(value = "classpath:build.properties", ignoreResourceNotFound = true)
public class StandaloneApplication {
    public static void main(String[] args) {
        SpringApplication.run(StandaloneApplication.class, args);
    }

    @Bean
    public ImageServerCoreModule imageServerCoreModule() {
        ImageServerCoreModule coreModule = new ImageServerCoreModule();
        coreModule.setProperty(ImageServerCoreModuleSettings.IMAGE_STORE_FOLDER,
                new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString()));
        coreModule.setProperty(ImageServerCoreModuleSettings.PROVIDE_STACKTRACE, true);
        coreModule.setProperty(ImageServerCoreModuleSettings.IMAGEMAGICK_ENABLED, true);
        coreModule.setProperty(ImageServerCoreModuleSettings.IMAGEMAGICK_USE_GRAPHICSMAGICK, true);
        coreModule.setProperty(ImageServerCoreModuleSettings.ROOT_PATH, "/resources/images");
        coreModule.setProperty(ImageServerCoreModuleSettings.ACCESS_TOKEN, "standalone-access-token");

        return coreModule;
    }

    @Bean
    public ImageServerAdminWebModule imageServerAdminModule() {
        ImageServerAdminWebModule imageServerAdminWebModule = new ImageServerAdminWebModule();
        imageServerAdminWebModule.setProperty(ImageServerAdminWebModuleSettings.IMAGE_SERVER_URL,
                "/resources/images");
        imageServerAdminWebModule.setProperty(ImageServerAdminWebModuleSettings.ACCESS_TOKEN,
                "standalone-access-token");

        return imageServerAdminWebModule;
    }
}