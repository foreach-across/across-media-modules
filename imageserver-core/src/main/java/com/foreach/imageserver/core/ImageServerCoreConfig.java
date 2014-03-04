package com.foreach.imageserver.core;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.StoredImageVariant;
import com.foreach.imageserver.core.web.interceptors.GlobalVariableInterceptor;
import liquibase.integration.spring.SpringLiquibase;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = "com.foreach.imageserver.core")
@MapperScan("com.foreach.imageserver.core.data")
public class ImageServerCoreConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    @Exposed
    public RequestMappingHandlerMapping imageServerCoreHandlerMapping() {
        RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
        handlerMapping.setInterceptors(new Object[]{globalVariableInterceptor()});
        return handlerMapping;
    }

    @Bean
    public GlobalVariableInterceptor globalVariableInterceptor() {
        return new GlobalVariableInterceptor();
    }

//    @Bean
//    public WebContentInterceptor cachingInterceptor() {
//        WebContentInterceptor interceptor = new WebContentInterceptor();
//        interceptor.setCacheSeconds(0);
//        interceptor.setUseExpiresHeader(true);
//        interceptor.setUseCacheControlHeader(true);
//        interceptor.setUseCacheControlNoStore(true);
//
//        return interceptor;
//    }
//
//    @Bean
//    public UrlBasedViewResolver viewResolver() {
//        UrlBasedViewResolver viewResolver = new UrlBasedViewResolver();
//        viewResolver.setViewClass(JstlView.class);
//        viewResolver.setPrefix("/WEB-INF/jsp/");
//        viewResolver.setRedirectContextRelative(true);
//        viewResolver.setSuffix(".jsp");
//        return viewResolver;
//    }

    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public org.apache.ibatis.session.SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setTypeAliases(new Class[]{Application.class, Image.class, StoredImageVariant.class});
        return sessionFactory.getObject();
    }

    @Bean
    public SpringLiquibase springLiquibase(DataSource dataSource) {
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setDataSource(dataSource);
        springLiquibase.setChangeLog("classpath:com/foreach/imageserver/core/liquibase/changelog.xml");
        return springLiquibase;
    }

}
