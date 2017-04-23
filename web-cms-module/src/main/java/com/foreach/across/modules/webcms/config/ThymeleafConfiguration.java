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
package com.foreach.across.modules.webcms.config;

import com.foreach.across.modules.web.ui.thymeleaf.ViewElementModelWriterRegistry;
import com.foreach.across.modules.webcms.domain.component.model.WebComponentModel;
import com.foreach.across.modules.webcms.web.thymeleaf.WebComponentModelViewElementModelWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(ViewElementModelWriterRegistry.class)
@RequiredArgsConstructor
public class ThymeleafConfiguration
{
	private final WebComponentModelViewElementModelWriter modelViewElementModelWriter;

	@Autowired
	public void registerViewElements( ViewElementModelWriterRegistry modelWriterRegistry ) {
		modelWriterRegistry.registerModelWriter( WebComponentModel.class.getSimpleName(), modelViewElementModelWriter );
	}
}