package com.foreach.imageserver.core;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.imageserver.core.web.interceptors.GlobalVariableInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class ImageServerAcrossConfig {

    @Autowired
    private GlobalVariableInterceptor globalVariableInterceptor;

    /**
     * We omit this bean during integration testing as it causes the mock web requests to fail.
     */
    @Bean
    @Exposed
    public RequestMappingHandlerMapping imageServerCoreHandlerMapping() {
        RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
        handlerMapping.setInterceptors(new Object[]{globalVariableInterceptor});
        return handlerMapping;
    }

}
