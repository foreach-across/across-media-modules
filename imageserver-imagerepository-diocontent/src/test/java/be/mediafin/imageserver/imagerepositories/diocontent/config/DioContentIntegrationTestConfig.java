package be.mediafin.imageserver.imagerepositories.diocontent.config;

import be.mediafin.imageserver.imagerepositories.diocontent.DioContentImageRepositoryModule;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.filters.PackageBeanFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DioContentIntegrationTestConfig {

    @Bean
    public AcrossModule dioContentModule() {
        DioContentImageRepositoryModule module = new DioContentImageRepositoryModule();
        module.setExposeFilter(new PackageBeanFilter("be.mediafin.imageserver.imagerepositories.diocontent", "org.mybatis.spring.mapper"));
        return module;
    }

}
