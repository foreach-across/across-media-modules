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
package com.foreach.across.modules.webcms.web.thymeleaf;

import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;

import java.util.HashSet;
import java.util.Set;

/**
 * Custom Thymeleaf dialect supporting WebCms components.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class WebCmsDialect extends AbstractProcessorDialect
{
	public static final String PREFIX = "wcm";

	public WebCmsDialect() {
		super( "WebCms", PREFIX, StandardDialect.PROCESSOR_PRECEDENCE );
	}

	@Override
	public Set<IProcessor> getProcessors( final String dialectPrefix ) {
		Set<IProcessor> processors = new HashSet<>();
		processors.add( new WebComponentModelProcessor() );
		return processors;
	}
}
