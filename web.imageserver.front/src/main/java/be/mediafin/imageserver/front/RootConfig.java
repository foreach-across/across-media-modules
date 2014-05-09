package be.mediafin.imageserver.front;

import be.mediafin.imageserver.front.mfn.MfnImageServerFrontModule;
import com.foreach.across.core.AcrossContext;
import com.foreach.imageserver.core.ImageServerCoreModule;
import com.foreach.spring.logging.LogbackConfigurer;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
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
    public AcrossContext acrossContext(ConfigurableApplicationContext parentContext, DataSource dataSource) {
        AcrossContext context = new AcrossContext(parentContext);
        context.setAllowInstallers(true);
        context.setDataSource(dataSource);
        context.addPropertySources(parentContext.getEnvironment().getPropertySources());
        context.addModule(imageServerCoreModule());
        context.addModule(mfnImageServerFrontModule());
        return context;
    }

    @Bean
    public ImageServerCoreModule imageServerCoreModule() {
        return new ImageServerCoreModule();
    }

    @Bean
    public MfnImageServerFrontModule mfnImageServerFrontModule() {
        return new MfnImageServerFrontModule();
    }

}
