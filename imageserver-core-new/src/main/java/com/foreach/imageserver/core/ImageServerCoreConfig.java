package com.foreach.imageserver.core;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.imageserver.core.business.*;
import liquibase.integration.spring.SpringLiquibase;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
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
    public org.apache.ibatis.session.SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setTypeAliases(new Class[]{Context.class, Image.class, ImageResolution.class, ImageModification.class, WebImageParameters.class});
        return sessionFactory.getObject();
    }

    @Bean
    public SpringLiquibase springLiquibase(DataSource dataSource) {
        // TODO Should become an Across installer.
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setDataSource(dataSource);
        springLiquibase.setChangeLog("classpath:com/foreach/imageserver/core/liquibase/changelog.xml");
        return springLiquibase;
    }

}
