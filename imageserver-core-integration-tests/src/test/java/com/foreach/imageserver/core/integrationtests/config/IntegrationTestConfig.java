package com.foreach.imageserver.core.integrationtests.config;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.filters.PackageBeanFilter;
import com.foreach.imageserver.core.ImageServerCoreModule;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.sql.DataSource;

@Configuration
@EnableWebMvc
public class IntegrationTestConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(
            @Value("classpath:integrationtests.properties") Resource defaultProperties) {
        PropertySourcesPlaceholderConfigurer propertySources = new PropertySourcesPlaceholderConfigurer();
        propertySources.setLocation(defaultProperties);
        propertySources.setIgnoreResourceNotFound(true);
        propertySources.setIgnoreUnresolvablePlaceholders(true);

        return propertySources;
    }

    @Bean
    public DataSource dataSource(@Value("${jdbc.driver}") String driver,
                                 @Value("${jdbc.url}") String url,
                                 @Value("${jdbc.username}") String userName,
                                 @Value("${jdbc.password}") String password) {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(driver);
        ds.setUrl(url);
        ds.setUsername(userName);
        ds.setPassword(password);
        ds.setDefaultAutoCommit(true);

        return ds;
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public AcrossContext acrossContext(ApplicationContext parentContext, PropertySourcesPlaceholderConfigurer propertyConfigurer, DataSource dataSource) {
        AcrossContext context = new AcrossContext(parentContext);
        context.setAllowInstallers(true);
        context.setDataSource(dataSource);
        context.addPropertySources(propertyConfigurer);

        context.addModule(imageServerCoreModule());

        return context;
    }

    @Bean
    public ImageServerCoreModule imageServerCoreModule() {
        ImageServerCoreModule module = new ImageServerCoreModule();
        module.setExposeFilter(new PackageBeanFilter("com.foreach.imageserver.core", "org.mybatis.spring.mapper"));
        return module;
    }

}
