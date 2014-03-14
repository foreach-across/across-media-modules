package be.mediafin.imageserver.front;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.imageserver.connectors.dpp.DioContentModule;
import com.foreach.imageserver.core.ImageServerCoreModule;
import com.foreach.spring.logging.LogbackConfigurer;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.sql.DataSource;

@Configuration
@EnableWebMvc
@PropertySource("classpath:be/mediafin/imageserver/config/${environment.type}/common.properties")
public class RootConfig {

    @Bean
    public LogbackConfigurer logbackConfigurer(@Value("${log.dir}") String logDir,
                                               @Value("${log.config}") Resource baseConfig,
                                               @Value("${log.config.extend}") Resource envConfig) {
        return new LogbackConfigurer(logDir, baseConfig, envConfig);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propertySources = new PropertySourcesPlaceholderConfigurer();
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
    public AcrossContext acrossContext(ApplicationContext parentContext, PropertySourcesPlaceholderConfigurer propertyConfigurer, DataSource dataSource) {
        AcrossContext context = new AcrossContext(parentContext);
        context.setAllowInstallers(true);
        context.setDataSource(dataSource);
        context.addPropertySources(propertyConfigurer);

        context.addModule(imageServerCoreModule());
        //TODO: we probably don't want this dependency here:
        context.addModule(dioContentModule());

        return context;
    }

    @Bean
    public AcrossModule dioContentModule() {
        return new DioContentModule();
    }

    @Bean
    public ImageServerCoreModule imageServerCoreModule() {
        return new ImageServerCoreModule();
    }

}
