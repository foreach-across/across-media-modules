package be.mediafin.imageserver.imagerepositories.diocontent;

import be.mediafin.imageserver.imagerepositories.diocontent.business.DioContentImageParameters;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = "be.mediafin.imageserver.imagerepositories.diocontent", excludeFilters = @ComponentScan.Filter(Configuration.class))
@MapperScan("be.mediafin.imageserver.imagerepositories.diocontent.data")
public class DioContentImageRepositoryConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public static DioContentImageRepositoryCreator dioContentImageRepositoryCreator() {
        return new DioContentImageRepositoryCreator();
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
        sessionFactory.setTypeAliases(new Class[]{DioContentImageParameters.class});
        return sessionFactory.getObject();
    }

}
