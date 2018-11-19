package com.foreach.imageserver.core.config;

import com.foreach.across.modules.hibernate.jpa.repositories.config.EnableAcrossJpaRepositories;
import com.foreach.imageserver.core.repositories.ImageRepository;
import org.springframework.context.annotation.Configuration;

@EnableAcrossJpaRepositories(basePackageClasses = ImageRepository.class)
@Configuration
public class RepositoriesConfiguration
{

}
