package com.foreach.imageserver.core.annotations;

import com.foreach.across.core.annotations.AcrossEventHandler;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Used to indicate controllers that should hook under the ImageServer root path.
 *
 * @author Arne Vandamme
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Inherited
public @interface ImageServerController
{
}
