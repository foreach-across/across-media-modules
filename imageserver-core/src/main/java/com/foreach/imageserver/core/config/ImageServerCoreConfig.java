package com.foreach.imageserver.core.config;

import com.foreach.imageserver.core.services.ImageRepositoryRegistry;
import com.foreach.imageserver.core.transformers.ImageTransformerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.sql.DataSource;

@Configuration
@ComponentScan(
		basePackages = {"com.foreach.imageserver.core.managers" }
)
@EnableTransactionManagement
public class ImageServerCoreConfig extends WebMvcConfigurerAdapter
{
	@Autowired
	private DataSource dataSource;

	/*
	@Autowired(required = false)
	private MultipartResolver multipartResolver;

	@PostConstruct
	public void init() {
		if ( multipartResolver == null ) {
			throw new RuntimeException( "A MultipartResolver bean should be set up in the root application context." );
		}
	}
	*/

	/*
		@Override
		public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
			converters.add(mappingJackson2HttpMessageConverter());
		}

		@Bean
		public MappingJacksonHttpMessageConverter mappingJackson2HttpMessageConverter() {
			return new MappingJacksonHttpMessageConverter();
		}
	*/

	@Bean
	public ImageTransformerRegistry imageTransformerRegistry() {
		return new ImageTransformerRegistry();
	}

	@Bean
	public ImageRepositoryRegistry imageRepositoryRegistry() {
		return new ImageRepositoryRegistry();
	}
/*
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
*/
}
