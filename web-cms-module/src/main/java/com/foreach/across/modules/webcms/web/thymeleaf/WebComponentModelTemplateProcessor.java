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

import com.foreach.across.modules.webcms.domain.component.model.WebComponentAutoCreateQueue;
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
final class WebComponentModelTemplateProcessor implements IPostProcessor
{
	static final String START_INSTRUCTION = "auto-create-component-start";
	static final String STOP_INSTRUCTION = "auto-create-component-finish";

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
		private ITemplateHandler next;
		private ITemplateContext context;

		private boolean enabled = false;

		private Output output;
		private ArrayDeque<Output> tree = new ArrayDeque<>();

		private static class Output
		{
			private StringWriter buffer = new StringWriter( 1024 );
			private OutputTemplateHandler handler = new OutputTemplateHandler( buffer );
		}

		private WebComponentAutoCreateQueue queue;

		@Override
		public void setNext( ITemplateHandler next ) {
			this.next = next;
		}

		@Override
		public void setContext( ITemplateContext context ) {
			this.context = context;

			ApplicationContext appCtx = RequestContextUtils.findWebApplicationContext( ( (WebEngineContext) context ).getRequest() );
			queue = appCtx.getBean( WebComponentAutoCreateQueue.class );
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

			if ( enabled ) {
				output.handler.handleXMLDeclaration( xmlDeclaration );
			}
		}

		@Override
		public void handleDocType( IDocType docType ) {
			next.handleDocType( docType );

			if ( enabled ) {
				output.handler.handleDocType( docType );
			}
		}

		@Override
		public void handleCDATASection( ICDATASection cdataSection ) {
			next.handleCDATASection( cdataSection );

			if ( enabled ) {
				output.handler.handleCDATASection( cdataSection );
			}
		}

		@Override
		public void handleComment( IComment comment ) {
			next.handleComment( comment );

			if ( enabled ) {
				output.handler.handleComment( comment );
			}
		}

		@Override
		public void handleText( IText text ) {
			next.handleText( text );

			if ( enabled ) {
				output.handler.handleText( text );
			}
		}

		@Override
		public void handleStandaloneElement( IStandaloneElementTag standaloneElementTag ) {
			next.handleStandaloneElement( standaloneElementTag );

			if ( enabled ) {
				output.handler.handleStandaloneElement( standaloneElementTag );
			}
		}

		@Override
		public void handleOpenElement( IOpenElementTag openElementTag ) {
			next.handleOpenElement( openElementTag );

			if ( enabled ) {
				output.handler.handleOpenElement( openElementTag );
			}
		}

		@Override
		public void handleCloseElement( ICloseElementTag closeElementTag ) {
			next.handleCloseElement( closeElementTag );

			if ( enabled ) {
				output.handler.handleCloseElement( closeElementTag );
			}
		}

		@Override
		public void handleProcessingInstruction( IProcessingInstruction processingInstruction ) {
			if ( START_INSTRUCTION.equals( processingInstruction.getTarget() ) ) {
				queue.outputStarted( processingInstruction.getContent() );
				this.output = new Output();
				tree.push( this.output );

				enabled = true;
			}
			else if ( STOP_INSTRUCTION.equals( processingInstruction.getTarget() ) ) {
				queue.outputFinished( processingInstruction.getContent(), tree.pop().buffer.toString() );
				this.output = tree.peek();
				enabled = this.output != null;
			}
			else {
				next.handleProcessingInstruction( processingInstruction );
			}
		}
	}
}
