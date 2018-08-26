package com.foreach.across.samples.filemanager;

import com.foreach.across.config.AcrossApplication;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.filemanager.FileManagerModule;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.properties.PropertiesModule;
import com.foreach.across.modules.web.AcrossWebModule;
import org.springframework.boot.SpringApplication;

import java.util.Collections;

/**
 * @author Steven Gentens
 */
@AcrossApplication(modules = { AdminWebModule.NAME, FileManagerModule.NAME, AcrossWebModule.NAME, AcrossHibernateJpaModule.NAME, PropertiesModule.NAME, EntityModule.NAME })
public class FileManagerTestApplication {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(FileManagerTestApplication.class);
        springApplication.setDefaultProperties(Collections.singletonMap("spring.config.location", "${user.home}/dev-configs/fmm-test-application.yml"));
        springApplication.run(args);
    }

}
