package com.foreach.imageserver.core.config;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.filters.PackageBeanFilter;
import com.foreach.imageserver.core.ImageServerCoreModule;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.List;

@Configuration
@PropertySource("classpath:integrationtests.properties")
public class IntegrationTestConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propertySources = new PropertySourcesPlaceholderConfigurer();
        propertySources.setIgnoreResourceNotFound(false);
        propertySources.setIgnoreUnresolvablePlaceholders(false);

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
    public AcrossContext acrossContext(ApplicationContext parentContext, PropertySourcesPlaceholderConfigurer propertyConfigurer, DataSource dataSource, List<AcrossModule> acrossModules) {
        AcrossContext context = new AcrossContext(parentContext);
        context.setAllowInstallers(true);
        context.setDataSource(dataSource);
        context.addPropertySources(propertyConfigurer);

        for (AcrossModule acrossModule : acrossModules) {
            context.addModule(acrossModule);
        }

        return context;
    }

    @Bean
    public AcrossModule imageServerCoreModule() {
        ImageServerCoreModule module = new ImageServerCoreModule();
        module.setExposeFilter(new PackageBeanFilter("com.foreach.imageserver.core", "org.mybatis.spring.mapper"));
        return module;
    }

}
