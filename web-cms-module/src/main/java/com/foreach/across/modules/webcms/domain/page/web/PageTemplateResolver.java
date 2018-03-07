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

package com.foreach.across.modules.webcms.domain.page.web;

import com.foreach.across.core.DynamicAcrossModule;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.events.AcrossContextBootstrappedEvent;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Resolves a configured template name to the actual template.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Exposed
@Component
@EnableConfigurationProperties(PageTemplateProperties.class)
@RequiredArgsConstructor
@Slf4j
public class PageTemplateResolver
{
	private final PageTemplateProperties properties;

	public String resolvePageTemplate( WebCmsPage page ) {
		if ( StringUtils.isBlank( page.getTemplate() ) ) {
			if ( page.getPageType().getTemplate() != null ) {
				return resolvePageTemplate( page.getPageType().getTemplate() );
			}
			else {
				return properties.getDefaultTemplate();
			}

		}
		return resolvePageTemplate( page.getTemplate() );
	}

	/**
	 * Resolves the requested template.  Will apply the configuration properties from the registered
	 * {@link PageTemplateProperties} instance.  If the requested template is {@code null}, the default
	 * will be returned.  Else the requested template will be cleaned up according to the {@link #properties}.
	 *
	 * @param template requested
	 * @return resolved template
	 */
	public String resolvePageTemplate( String template ) {
		if ( StringUtils.isBlank( template ) ) {
			return properties.getDefaultTemplate();
		}

		if ( StringUtils.startsWithAny( template, properties.getTemplatePrefixToIgnore() ) ) {
			return StringUtils.removeEnd( template, properties.getTemplateSuffixToRemove() );
		}

		return StringUtils.removeEnd(
				StringUtils.replace(
						StringUtils.defaultString( properties.getTemplatePrefix() ) + template, "//", "/"
				),
				properties.getTemplateSuffixToRemove()
		);
	}

	/**
	 * If no default prefix is set but there is a {@link com.foreach.across.core.DynamicAcrossModule.DynamicApplicationModule}
	 * present, generate a Thymeleaf prefix for those resources.
	 */
	@EventListener
	void determineDefaultPrefix( AcrossContextBootstrappedEvent contextBootstrappedEvent ) {
		if ( properties.getTemplatePrefix() == null ) {
			contextBootstrappedEvent
					.getModules()
					.stream()
					.filter( m -> m.isBootstrapped() && m.getModule() instanceof DynamicAcrossModule )
					.findFirst()
					.map( AcrossModuleInfo::getModule )
					.ifPresent( m -> {
						            String prefix = "th/" + m.getResourcesKey() + "/";
						            LOG.info( "No default WebCmsPage template prefix configured - using {}", prefix );
						            properties.setTemplatePrefix( prefix );
					            }
					);
		}
	}
}
