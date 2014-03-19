package be.mediafin.imageserver.imagerepositories.diocontent;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class DioContentRepositoryConditional implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        String propertyValue = conditionContext.getEnvironment().getProperty("imagerepository.diocontent.enabled");
        return Boolean.valueOf(propertyValue);
    }

}
