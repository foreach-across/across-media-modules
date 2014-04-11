package com.foreach.imageserver.imagerepositories.web.config;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.filters.PackageBeanFilter;
import com.foreach.imageserver.imagerepositories.web.WebImageRepositoryModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebIntegrationTestConfig {

    @Bean
    public AcrossModule webModule() {
        WebImageRepositoryModule module = new WebImageRepositoryModule();
        module.setExposeFilter(new PackageBeanFilter("be.mediafin.imageserver.imagerepositories.web", "org.mybatis.spring.mapper"));
        return module;
    }

}
