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

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.across.modules.web.thymeleaf.ThymeleafModelBuilder;
import com.foreach.across.modules.webcms.domain.component.WebCmsContentMarker;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.regex.Matcher;

import static com.foreach.across.modules.webcms.domain.component.WebCmsContentMarkerService.MARKER_PATTERN;

/**
 * Writes text or HTML output to the Thymeleaf model while optionally replacing all content markers.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 * @see WebCmsContentMarker
 * @see com.foreach.across.modules.webcms.domain.component.WebCmsContentMarkerService
 */
@Exposed
@Component
public final class WebCmsComponentContentModelWriter
{
	private final WebCmsComponentContentMarkerRenderer KEEP_MARKER = new WebCmsComponentContentMarkerRenderer()
	{
		@Override
		public boolean supports( WebCmsComponentModel componentModel, WebCmsContentMarker marker ) {
			return false;
		}

		@Override
		public void writeMarkerOutput( WebCmsComponentModel component, WebCmsContentMarker marker, ThymeleafModelBuilder model ) {
			model.addText( marker.toString() );
		}
	};

	private Collection<WebCmsComponentContentMarkerRenderer> renderers;

	/**
	 * Write escaped text to the Thymeleaf output.
	 *
	 * @param componentModel being rendered
	 * @param text           to write
	 * @param parseMarkers   true if markers should be replaced
	 * @param model          to write the output to
	 */
	public void writeText( WebCmsComponentModel componentModel, String text, boolean parseMarkers, ThymeleafModelBuilder model ) {
		writeText( componentModel, text, true, parseMarkers, model );
	}

	/**
	 * Write raw unescaped text (HTML) to the Thymeleaf output.
	 *
	 * @param componentModel being rendered
	 * @param text           to write
	 * @param parseMarkers   true if markers should be replaced
	 * @param model          to write the output to
	 */
	public void writeHtml( WebCmsComponentModel componentModel, String text, boolean parseMarkers, ThymeleafModelBuilder model ) {
		writeText( componentModel, text, false, parseMarkers, model );
	}

	/**
	 * Write text to the Thymeleaf output.
	 *
	 * @param componentModel being rendered
	 * @param text           to write
	 * @param escapeXml      true if XML markup should be escaped
	 * @param parseMarkers   true if markers should be replaced
	 * @param model          to write the output to
	 */

	public void writeText( WebCmsComponentModel componentModel, String text, boolean escapeXml, boolean parseMarkers, ThymeleafModelBuilder model ) {
		if ( parseMarkers ) {
			Matcher matcher = MARKER_PATTERN.matcher( text );

			int start = 0;

			while ( matcher.find() ) {
				model.addText( text.substring( start, matcher.start() ), escapeXml );
				writeMarker( componentModel, WebCmsContentMarker.fromMarkerString( matcher.group() ), model );
				start = matcher.end();
			}

			model.addText( text.substring( start ), escapeXml );
		}
		else {
			model.addText( text, escapeXml );
		}
	}

	@SuppressWarnings("unchecked")
	private void writeMarker( WebCmsComponentModel componentModel, WebCmsContentMarker contentMarker, ThymeleafModelBuilder model ) {
		renderers.stream()
		         .filter( r -> r.supports( componentModel, contentMarker ) )
		         .findFirst()
		         .orElse( KEEP_MARKER )
		         .writeMarkerOutput( componentModel, contentMarker, model );
	}

	@Autowired
	void setRenderers( @RefreshableCollection(includeModuleInternals = true) Collection<WebCmsComponentContentMarkerRenderer> renderers ) {
		this.renderers = renderers;
	}
}
