package com.foreach.imageserver.core.config;

import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;

@Configuration
@Import(DataConfig.class)
public class TestDataConfig {
    @Bean
    public DataSource dataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl("jdbc:h2:mem:image-server-unit-tests;DB_CLOSE_DELAY=-1;MODE=MSSQLServer");
        ds.setUsername("sa");
        ds.setPassword("");

        return ds;
    }

    @Bean
    public SpringLiquibase liquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource());
        liquibase.setChangeLog("classpath:/liquibase/changelog-ut.xml");
        liquibase.setContexts("test");

        return liquibase;
    }
}
