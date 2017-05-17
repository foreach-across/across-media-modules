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

import com.foreach.across.modules.webcms.domain.component.placeholder.WebCmsPlaceholderContentModel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.WebEngineContext;
import org.thymeleaf.engine.AbstractTemplateHandler;
import org.thymeleaf.engine.ITemplateHandler;
import org.thymeleaf.engine.OutputTemplateHandler;
import org.thymeleaf.model.*;
import org.thymeleaf.postprocessor.IPostProcessor;
import org.thymeleaf.templatemode.TemplateMode;

import java.io.StringWriter;
import java.util.ArrayDeque;

/**
 * Supports the placeholder related processing instructions.  Increases and decreases the level
 * if placeholder parsing is (de-)activated.  If placeholders are being parsed and a placeholder block
 * is initiated, it will be rendered to a string that is stored in the {@link WebCmsPlaceholderContentModel}
 * instead of being sent to the template output.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
final class PlaceholderTemplatePostProcessor implements IPostProcessor
{
	/**
	 * Marks block of events that make up a single placeholder.
	 */
	static final String START_PLACEHOLDER = "render-to-placeholder-start";
	static final String STOP_PLACEHOLDER = "render-to-placeholder-finish";

	/**
	 * Marks block of events that make up a single placeholder level.  Placeholder level
	 * will increment at the beginning and decrement at the end.
	 */
	static final String START_PARSE_PLACEHOLDERS = "parse-placeholders-start";
	static final String STOP_PARSE_PLACEHOLDERS = "parse-placeholders-stop";

	/**
	 * Marks block of events in which only events between placeholder markers should be considered,
	 * all others should be ignored.
	 */
	static final String START_IGNORE_NON_PLACEHOLDERS = "ignore-non-placeholders-start";
	static final String STOP_IGNORE_NON_PLACEHOLDERS = "ignore-non-placeholders-stop";

	@Override
	public TemplateMode getTemplateMode() {
		return TemplateMode.HTML;
	}

	@Override
	public int getPrecedence() {
		return Integer.MAX_VALUE - 1;
	}

	@Override
	public Class<? extends ITemplateHandler> getHandlerClass() {
		return TemplateHandler.class;
	}

	public static class TemplateHandler implements ITemplateHandler
	{
		private final ITemplateHandler trashHandler = new AbstractTemplateHandler()
		{
		};

		private ITemplateHandler next, outputHandler;

		private boolean buildingPlaceholderContent = false;
		private PlaceholderContent placeholderContent;
		private final ArrayDeque<PlaceholderContent> tree = new ArrayDeque<>();

		private WebCmsPlaceholderContentModel placeholderContentModel;

		private int level = 0;
		private int trashLevel = 0;

		@Override
		public void setNext( ITemplateHandler next ) {
			this.next = next;
			this.outputHandler = next;
		}

		@Override
		public void setContext( ITemplateContext context ) {
			ApplicationContext appCtx = RequestContextUtils.findWebApplicationContext( ( (WebEngineContext) context ).getRequest() );
			placeholderContentModel = appCtx.getBean( WebCmsPlaceholderContentModel.class );
		}

		@Override
		public void handleTemplateStart( ITemplateStart templateStart ) {
			next.handleTemplateStart( templateStart );
		}

		@Override
		public void handleTemplateEnd( ITemplateEnd templateEnd ) {
			next.handleTemplateEnd( templateEnd );
		}

		@Override
		public void handleXMLDeclaration( IXMLDeclaration xmlDeclaration ) {
			next.handleXMLDeclaration( xmlDeclaration );
		}

		@Override
		public void handleDocType( IDocType docType ) {
			next.handleDocType( docType );
		}

		@Override
		public void handleCDATASection( ICDATASection cdataSection ) {
			next.handleCDATASection( cdataSection );
		}

		@Override
		public void handleComment( IComment comment ) {
			next.handleComment( comment );
		}

		@Override
		public void handleText( IText text ) {
			next.handleText( text );
		}

		@Override
		public void handleStandaloneElement( IStandaloneElementTag standaloneElementTag ) {
			next.handleStandaloneElement( standaloneElementTag );
		}

		@Override
		public void handleOpenElement( IOpenElementTag openElementTag ) {
			next.handleOpenElement( openElementTag );
		}

		@Override
		public void handleCloseElement( ICloseElementTag closeElementTag ) {
			next.handleCloseElement( closeElementTag );
		}

		@Override
		public void handleProcessingInstruction( IProcessingInstruction processingInstruction ) {
			if ( ComponentTemplatePostProcessor.COMPONENT_RENDER.equals( processingInstruction.getTarget() )) {
				// suppress
			}
			else if ( START_PLACEHOLDER.equals( processingInstruction.getTarget() ) && parsing() ) {
				this.placeholderContent = new PlaceholderContent( processingInstruction.getContent() );
				tree.push( this.placeholderContent );

				buildingPlaceholderContent = true;
				next = placeholderContent.handler;
			}
			else if ( STOP_PLACEHOLDER.equals( processingInstruction.getTarget() ) && parsing() ) {
				PlaceholderContent placeholderContent = tree.pop();
				placeholderContentModel.setPlaceholderContent( placeholderContent.placeholder, placeholderContent.buffer.toString() );

				this.placeholderContent = tree.peek();
				buildingPlaceholderContent = this.placeholderContent != null;
				next = buildingPlaceholderContent ? placeholderContent.handler : ( allowNonPlaceholderOutput() ? outputHandler : trashHandler );
			}
			else if ( START_PARSE_PLACEHOLDERS.equals( processingInstruction.getTarget() ) ) {
				level++;
				placeholderContentModel.increaseLevel();
			}
			else if ( STOP_PARSE_PLACEHOLDERS.equals( processingInstruction.getTarget() ) ) {
				level = Math.max( level - 1, 0 );
				placeholderContentModel.decreaseLevel();
			}
			else if ( START_IGNORE_NON_PLACEHOLDERS.equals( processingInstruction.getTarget() ) ) {
				trashLevel++;
				if ( !buildingPlaceholderContent ) {
					next = trashHandler;
				}
			}
			else if ( STOP_IGNORE_NON_PLACEHOLDERS.equals( processingInstruction.getTarget() ) ) {
				trashLevel = Math.max( trashLevel - 1, 0 );
				if ( /*!buildingPlaceholderContent*/ trashLevel == 0 ) {
					next = outputHandler;
				}
			}
			else {
				next.handleProcessingInstruction( processingInstruction );
			}
		}

		private boolean allowNonPlaceholderOutput() {
			return trashLevel == 0;
		}

		private boolean parsing() {
			return level > 0;
		}

		@RequiredArgsConstructor
		private static class PlaceholderContent
		{
			private final String placeholder;
			private final StringWriter buffer = new StringWriter( 1024 );
			private final ITemplateHandler handler = new OutputTemplateHandler( buffer );
		}
	}
}
