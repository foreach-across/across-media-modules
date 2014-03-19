package be.mediafin.imageserver.imagerepositories.diocontent;

import be.mediafin.imageserver.imagerepositories.diocontent.business.DioContentImageParameters;
import liquibase.integration.spring.SpringLiquibase;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = "be.mediafin.imageserver.imagerepositories.diocontent", excludeFilters = @ComponentScan.Filter(Configuration.class))
@MapperScan("be.mediafin.imageserver.imagerepositories.diocontent.data")
public class ImageServerDioContentConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public org.apache.ibatis.session.SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setTypeAliases(new Class[]{DioContentImageParameters.class});
        return sessionFactory.getObject();
    }

    @Bean
    public SpringLiquibase springLiquibase(DataSource dataSource) {
        // TODO Should become an Across installer.
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setDataSource(dataSource);
        springLiquibase.setChangeLog("classpath:be/mediafin/imageserver/imagerepositories/diocontent/liquibase/changelog.xml");
        return springLiquibase;
    }

}
