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

import com.foreach.across.modules.webcms.web.WebCmsRenderUtilityService;
import org.springframework.beans.factory.BeanFactory;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.dialect.IPostProcessorDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.postprocessor.IPostProcessor;
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
public class WebCmsDialect extends AbstractProcessorDialect implements IPostProcessorDialect, IExpressionObjectDialect
{
	public static final String PREFIX = "wcm";

	private final BeanFactory beanFactory;

	public WebCmsDialect( BeanFactory beanFactory ) {
		super( "WebCms", PREFIX, StandardDialect.PROCESSOR_PRECEDENCE );
		this.beanFactory = beanFactory;
	}

	@Override
	public Set<IProcessor> getProcessors( final String dialectPrefix ) {
		Set<IProcessor> processors = new HashSet<>();
		processors.add( new PlaceholderAttributeProcessor() );
		processors.add( new ComponentAttributesProcessor() );
		return processors;
	}

	@Override
	public int getDialectPostProcessorPrecedence() {
		return Integer.MAX_VALUE;
	}

	@Override
	public Set<IPostProcessor> getPostProcessors() {
		Set<IPostProcessor> processors = new HashSet<>();
		processors.add( new PlaceholderTemplatePostProcessor() );
		processors.add( new ComponentTemplatePostProcessor() );
		return processors;
	}

	@Override
	public IExpressionObjectFactory getExpressionObjectFactory() {
		return new AcrossExpressionObjectFactory();
	}

	public class AcrossExpressionObjectFactory implements IExpressionObjectFactory
	{
		@Override
		public Set<String> getAllExpressionObjectNames() {
			HashSet<String> names = new HashSet<>();
			names.add( PREFIX );
			return names;
		}

		@Override
		public Object buildObject( IExpressionContext context, String expressionObjectName ) {
			if ( PREFIX.equals( expressionObjectName ) ) {
				return beanFactory.getBean( WebCmsRenderUtilityService.class );
			}

			return null;
		}

		@Override
		public boolean isCacheable( String expressionObjectName ) {
			return true;
		}
	}
}
