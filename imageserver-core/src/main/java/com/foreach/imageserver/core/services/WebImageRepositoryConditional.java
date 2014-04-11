package com.foreach.imageserver.core.services;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class WebImageRepositoryConditional implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        String propertyValue = conditionContext.getEnvironment().getProperty("imagerepository.web.enabled");
        return Boolean.valueOf(propertyValue);
    }

}
