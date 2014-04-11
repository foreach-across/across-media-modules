package be.mediafin.imageserver.imagerepositories.diocontent;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

public class DioContentImageRepositoryCreator implements BeanFactoryPostProcessor, EnvironmentAware {
    private Environment environment;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory context) throws BeansException {
        BeanDefinitionRegistry registry = ((BeanDefinitionRegistry) context);

        String[] repositoryNames = obtainRepositoryNames();
        for (String repositoryName : repositoryNames) {
            GenericBeanDefinition definition = new GenericBeanDefinition();
            definition.setBeanClass(DioContentImageRepository.class);
            definition.setAutowireCandidate(true);
            definition.setConstructorArgumentValues(obtainConstructorArgumentValues(repositoryName));
            registry.registerBeanDefinition("dioContentImageRepository_" + repositoryName, definition);
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private String[] obtainRepositoryNames() throws BeansException {
        String packedRepositoryNames = environment.getProperty("imagerepository.diocontent.repositories");
        if (StringUtils.isBlank(packedRepositoryNames)) {
            throw new BeanInitializationException("Property \"imagerepository.diocontent.repositories\" should contain a comma-separated list of repository names.");
        }
        return StringUtils.split(packedRepositoryNames, ',');
    }

    private ConstructorArgumentValues obtainConstructorArgumentValues(String repositoryName) {
        ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
        constructorArgumentValues.addIndexedArgumentValue(0, expectRepositoryValue(repositoryName, "serverUrl"));
        constructorArgumentValues.addIndexedArgumentValue(1, expectRepositoryValue(repositoryName, "username"));
        constructorArgumentValues.addIndexedArgumentValue(2, expectRepositoryValue(repositoryName, "password"));
        constructorArgumentValues.addIndexedArgumentValue(3, repositoryName);
        return constructorArgumentValues;
    }

    private String expectRepositoryValue(String repositoryName, String key) {
        StringBuilder builder = new StringBuilder();
        builder.append("imagerepository.diocontent.");
        builder.append(repositoryName);
        builder.append(".");
        builder.append(key);

        String value = environment.getProperty(builder.toString());
        if (StringUtils.isBlank(value)) {
            throw new BeanInitializationException(String.format("Expected non-blank value for property \"%s\".", builder.toString()));
        }
        return value;
    }
}
