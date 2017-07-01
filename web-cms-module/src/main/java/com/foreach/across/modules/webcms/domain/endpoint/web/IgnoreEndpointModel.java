/*
 * Copyright 2017 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.modules.webcms.domain.endpoint.web;

import java.lang.annotation.*;

/**
 * When used on a {@link org.springframework.stereotype.Controller} or handler method
 * that maps to a {@link com.foreach.across.modules.webcms.domain.endpoint.web.context.WebCmsEndpointContext},
 * this annotation will ensure the {@link WebCmsEndpointModelLoader} beans will not be called and no default model will be created.
 * <p/>
 * Use this if you want to build the entire model inside your handler method.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 * @see com.foreach.across.modules.webcms.domain.endpoint.web.interceptor.WebCmsEndpointHandlerInterceptor
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface IgnoreEndpointModel
{
}
