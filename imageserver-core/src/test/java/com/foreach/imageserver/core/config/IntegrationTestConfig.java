package com.foreach.imageserver.core.config;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.filters.PackageBeanFilter;
import com.foreach.across.core.installers.InstallerAction;
import com.foreach.across.modules.hibernate.AcrossHibernateModule;
import com.foreach.imageserver.core.ImageServerCoreModule;
import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

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
    public MultipartResolver multipartResolver() {
        return new CommonsMultipartResolver();
    }

    @Bean
    public AcrossContext acrossContext(ConfigurableApplicationContext parentContext, DataSource dataSource, List<AcrossModule> acrossModules) {
        AcrossContext context = new AcrossContext(parentContext);
        context.setInstallerAction(InstallerAction.EXECUTE);
        context.setDataSource(dataSource);
        context.addPropertySources(parentContext.getEnvironment().getPropertySources());

        for (AcrossModule acrossModule : acrossModules) {
            context.addModule(acrossModule);
        }

        return context;
    }

    @Bean
    public AcrossModule dummyWebModule() {
        return new EmptyAcrossModule("AcrossWebModule");
    }

    @Bean
    public AcrossModule imageServerCoreModule() {
        ImageServerCoreModule module = new ImageServerCoreModule();
	    module.setProperty( ImageServerCoreModuleSettings.IMAGE_STORE_FOLDER,
	                                       System.getProperty( "java.io.tmpdir" ) );
        module.setExposeFilter(new PackageBeanFilter("com.foreach.imageserver.core", "org.mybatis.spring.mapper", "net.sf.ehcache"));
        return module;
    }

	@Bean
	public AcrossModule acrossHibernateModule() {
		return new AcrossHibernateModule();
	}

}
