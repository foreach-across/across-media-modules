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
import org.thymeleaf.engine.ITemplateHandler;
import org.thymeleaf.engine.OutputTemplateHandler;
import org.thymeleaf.model.*;
import org.thymeleaf.postprocessor.IPostProcessor;
import org.thymeleaf.templatemode.TemplateMode;

import java.io.StringWriter;
import java.util.ArrayDeque;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
final class PlaceholderTemplateProcessor implements IPostProcessor
{
	static final String START_INSTRUCTION = "render-to-placeholder-start";
	static final String STOP_INSTRUCTION = "render-to-placeholder-finish";
	static final String DECREASE_PLACEHOLDER_LEVEL = "decrease-placeholder-level";

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
		private ITemplateHandler next, original;
		private ITemplateContext context;

		private boolean enabled = false;

		private Output output;
		private ArrayDeque<Output> tree = new ArrayDeque<>();

		private WebCmsPlaceholderContentModel placeholderContentModel;

		@RequiredArgsConstructor
		private static class Output
		{
			private final String placeholder;
			private final StringWriter buffer = new StringWriter( 1024 );
			private final OutputTemplateHandler handler = new OutputTemplateHandler( buffer );
		}

		@Override
		public void setNext( ITemplateHandler next ) {
			this.next = next;
			this.original = next;
		}

		@Override
		public void setContext( ITemplateContext context ) {
			this.context = context;

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
			if ( START_INSTRUCTION.equals( processingInstruction.getTarget() ) ) {
				//queue.outputStarted( processingInstruction.getContent() );
				this.output = new Output( processingInstruction.getContent() );
				tree.push( this.output );

				enabled = true;
				next = output.handler;
			}
			else if ( STOP_INSTRUCTION.equals( processingInstruction.getTarget() ) ) {
				tree.pop();
				//System.err.println( "{" + tree.pop().buffer.toString()+ "}" );
				//queue.outputFinished( processingInstruction.getContent(), tree.pop().buffer.toString() );
				this.output = tree.peek();
				enabled = this.output != null;
				next = enabled ? output.handler : original;
			}
			else if ( DECREASE_PLACEHOLDER_LEVEL.equals( processingInstruction.getTarget() ) ) {
				placeholderContentModel.decreaseLevel();
			}
			else {
				next.handleProcessingInstruction( processingInstruction );
			}
		}
	}
}
