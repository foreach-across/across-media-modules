package com.foreach.imageserver.core.transformers;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ImageMagickImageTransformerConditional implements Condition {
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        String propertyValue = conditionContext.getEnvironment().getProperty("transformer.imagemagick.enabled");
        return Boolean.valueOf(propertyValue);
    }
}
