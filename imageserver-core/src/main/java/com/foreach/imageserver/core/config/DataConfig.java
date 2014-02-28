package com.foreach.imageserver.core.config;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageModification;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.dbcp.BasicDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan("com.foreach.imageserver.data")
public class DataConfig
{
	@Value("${jdbc.driver}")
	private String driver;

	@Value("${jdbc.url}")
	private String url;

	@Value("${jdbc.username}")
	private String userName;

	@Value("${jdbc.password}")
	private String password;

	@Bean
	public DataSource dataSource() {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName( driver );
		ds.setUrl( url );
		ds.setUsername( userName );
		ds.setPassword( password );
		ds.setDefaultAutoCommit( true );

		return ds;
	}

	@Bean
	public DataSourceTransactionManager transactionManager() {
		return new DataSourceTransactionManager( dataSource() );
	}

	@Bean
	public org.apache.ibatis.session.SqlSessionFactory sqlSessionFactory() throws Exception {
		SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
		sessionFactory.setDataSource( dataSource() );
		sessionFactory.setTypeAliases( new Class[] { Application.class, Image.class, ImageModification.class } );
		return sessionFactory.getObject();
	}

    @Bean
    public SpringLiquibase springLiquibase( DataSource dataSource ) {
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setDataSource( dataSource );
        springLiquibase.setChangeLog( "classpath:liquibase/changelog.xml" );
        return springLiquibase;
    }

}
