package com.foreach.imageserver.core.config;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.StoredImageModification;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.dbcp.BasicDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@MapperScan("com.foreach.imageserver.core.data")
public class TestDataConfig {
    @Bean
    public DataSource dataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl("jdbc:hsqldb:mem:imageServer;sql.syntax_mss=true");
        ds.setUsername("sa");
        ds.setPassword("");
        return ds;
    }

    @Bean
    public org.apache.ibatis.session.SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setTypeAliases(new Class[]{Application.class, Image.class, StoredImageModification.class});
        return sessionFactory.getObject();
    }

    @Bean
    public SpringLiquibase liquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource());
        liquibase.setChangeLog("classpath:com/foreach/imageserver/core/liquibase/changelog-ut.xml");
        liquibase.setContexts("test");
        return liquibase;
    }
}
