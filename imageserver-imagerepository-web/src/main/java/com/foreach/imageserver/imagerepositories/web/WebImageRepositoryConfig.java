package com.foreach.imageserver.imagerepositories.web;

import com.foreach.imageserver.imagerepositories.web.business.WebImageParameters;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = "com.foreach.imageserver.imagerepositories.web", excludeFilters = @ComponentScan.Filter(Configuration.class))
@MapperScan("com.foreach.imageserver.imagerepositories.web.data")
public class WebImageRepositoryConfig {

    @Autowired
    private DataSource dataSource;

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
        sessionFactory.setTypeAliases(new Class[]{WebImageParameters.class});
        return sessionFactory.getObject();
    }

}
