package com.foreach.imageserver.core;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.services.ImageRepositoryRegistry;
import com.foreach.imageserver.core.transformers.ImageTransformerRegistry;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = "com.foreach.imageserver.core", excludeFilters = @ComponentScan.Filter(Configuration.class))
@MapperScan("com.foreach.imageserver.core.data")
@EnableTransactionManagement
public class ImageServerCoreConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    @Exposed
    public RequestMappingHandlerMapping imageServerCoreHandlerMapping() {
        RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
        handlerMapping.setInterceptors(new Object[]{});
        return handlerMapping;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propertySources = new PropertySourcesPlaceholderConfigurer();
        propertySources.setIgnoreResourceNotFound(false);
        propertySources.setIgnoreUnresolvablePlaceholders(false);

        return propertySources;
    }

    @Bean
    public org.apache.ibatis.session.SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setTypeAliases(new Class[]{Context.class, Image.class, ImageResolution.class, ImageModification.class, WebImageParameters.class});
        return sessionFactory.getObject();
    }

    @Bean
    public ImageTransformerRegistry imageTransformerRegistry() {
        return new ImageTransformerRegistry();
    }

    @Bean
    public ImageRepositoryRegistry imageRepositoryRegistry() {
        return new ImageRepositoryRegistry();
    }

}
