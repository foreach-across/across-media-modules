package com.foreach.imageserver.core;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.imageserver.core.business.Context;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.services.ImageRepositoryRegistry;
import com.foreach.imageserver.core.transformers.ImageTransformerRegistry;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = "com.foreach.imageserver.core", excludeFilters = @ComponentScan.Filter(Configuration.class))
@MapperScan("com.foreach.imageserver.core.data")
@EnableTransactionManagement
@EnableCaching
public class ImageServerCoreConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ImageServerCoreModule imageServerCoreModule;

    @Autowired(required = false)
    private MultipartResolver multipartResolver;

    @PostConstruct
    public void init() {
        if (multipartResolver == null) {
            throw new RuntimeException("A MultipartResolver bean should be set up in the root application context.");
        }
    }

    @Bean
    @Exposed
    public RequestMappingHandlerMapping imageServerCoreHandlerMapping() {
        RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
        handlerMapping.setInterceptors(new Object[]{});
        return handlerMapping;
    }

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
        sessionFactory.setTypeAliases(new Class[]{Context.class, Image.class, ImageResolution.class, ImageModification.class});
        return sessionFactory.getObject();
    }

    @Bean
    public ImageTransformerRegistry imageTransformerRegistry() {
        return new ImageTransformerRegistry();
    }

    @Bean
    public ImageRepositoryRegistry imageRepositoryRegistry() {
        return new ImageRepositoryRegistry();
    }

    @Bean
    public net.sf.ehcache.CacheManager ehCacheManager() {
        net.sf.ehcache.config.Configuration configuration = new net.sf.ehcache.config.Configuration();
        configuration.setName(imageServerCoreModule.getName());
        configuration.addCache(cacheConfiguration("contexts", 50, true));
        configuration.addCache(cacheConfiguration("imageResolutions", 100, true));
        configuration.addCache(cacheConfiguration("images", 5000, true));
        configuration.setUpdateCheck(false);
        return net.sf.ehcache.CacheManager.newInstance(configuration);
    }

    @Bean
    public CacheManager cacheManager(net.sf.ehcache.CacheManager ehCacheManager) {
        return new EhCacheCacheManager(ehCacheManager);
    }

    private static net.sf.ehcache.config.CacheConfiguration cacheConfiguration(String name, int maxEntriesOnHeap, boolean eternal) {
        net.sf.ehcache.config.CacheConfiguration cacheConfiguration = new net.sf.ehcache.config.CacheConfiguration();
        cacheConfiguration.setName(name);
        cacheConfiguration.setMemoryStoreEvictionPolicy("LRU");
        cacheConfiguration.setMaxEntriesLocalHeap(maxEntriesOnHeap);
        cacheConfiguration.setEternal(eternal);
        cacheConfiguration.freezeConfiguration();
        return cacheConfiguration;
    }

}
