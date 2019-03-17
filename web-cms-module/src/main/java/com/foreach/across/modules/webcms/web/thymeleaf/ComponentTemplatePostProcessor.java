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

import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateQueue;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.ITemplateHandler;
import org.thymeleaf.engine.OutputTemplateHandler;
import org.thymeleaf.model.*;
import org.thymeleaf.postprocessor.IPostProcessor;
import org.thymeleaf.templatemode.TemplateMode;

import java.io.StringWriter;
import java.util.ArrayDeque;

/**
 * Responsible for catching component output during auto-creation of components.
 *
 * @author Arne Vandamme
 * @see PlaceholderAttributeProcessor
 * @see ComponentAttributeProcessor
 * @see PlaceholderTemplatePostProcessor
 * @since 0.0.2
 */
final class ComponentTemplatePostProcessor implements IPostProcessor
{
	static final String START_INSTRUCTION = "auto-create-component-start";
	static final String STOP_INSTRUCTION = "auto-create-component-finish";

	static final String COMPONENT_RENDER = "component-render";
	static final String EXISTING_COMPONENT_PROXY = "component-proxy-by-componentId";
	static final String CREATED_COMPONENT_PROXY = "component-proxy-by-taskId";

	@Override
	public TemplateMode getTemplateMode() {
		return TemplateMode.HTML;
	}

	@Override
	public int getPrecedence() {
		return Integer.MAX_VALUE - 2;
	}

	@Override
	public Class<? extends ITemplateHandler> getHandlerClass() {
		return TemplateHandler.class;
	}

	public static class TemplateHandler implements ITemplateHandler
	{
		private ITemplateHandler next;
		private IModelFactory modelFactory;

		private boolean componentInCreation = false;

		private Output output;
		private ArrayDeque<Output> tree = new ArrayDeque<>();

		private static class Output
		{
			private StringWriter buffer = new StringWriter( 1024 );
			private OutputTemplateHandler handler = new OutputTemplateHandler( buffer );
		}

		private ModelProcessingState modelProcessingState;
		private WebCmsComponentAutoCreateQueue autoCreateQueue;

		@Override
		public void setNext( ITemplateHandler next ) {
			this.next = next;
		}

		@Override
		public void setContext( ITemplateContext context ) {
			modelFactory = context.getModelFactory();

			modelProcessingState = ModelProcessingState.retrieve( context );
			autoCreateQueue = modelProcessingState.getAutoCreateQueue();
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

			if ( componentInCreation ) {
				output.handler.handleXMLDeclaration( xmlDeclaration );
			}
		}

		@Override
		public void handleDocType( IDocType docType ) {
			next.handleDocType( docType );

			if ( componentInCreation ) {
				output.handler.handleDocType( docType );
			}
		}

		@Override
		public void handleCDATASection( ICDATASection cdataSection ) {
			next.handleCDATASection( cdataSection );

			if ( componentInCreation ) {
				output.handler.handleCDATASection( cdataSection );
			}
		}

		@Override
		public void handleComment( IComment comment ) {
			next.handleComment( comment );

			if ( componentInCreation ) {
				output.handler.handleComment( comment );
			}
		}

		@Override
		public void handleText( IText text ) {
			next.handleText( text );

			if ( componentInCreation ) {
				output.handler.handleText( text );
			}
		}

		@Override
		public void handleStandaloneElement( IStandaloneElementTag standaloneElementTag ) {
			next.handleStandaloneElement( standaloneElementTag );

			if ( componentInCreation ) {
				output.handler.handleStandaloneElement( standaloneElementTag );
			}
		}

		@Override
		public void handleOpenElement( IOpenElementTag openElementTag ) {
			next.handleOpenElement( openElementTag );

			if ( componentInCreation ) {
				output.handler.handleOpenElement( openElementTag );
			}
		}

		@Override
		public void handleCloseElement( ICloseElementTag closeElementTag ) {
			next.handleCloseElement( closeElementTag );

			if ( componentInCreation ) {
				output.handler.handleCloseElement( closeElementTag );
			}
		}

		@Override
		public void handleProcessingInstruction( IProcessingInstruction processingInstruction ) {
			if ( COMPONENT_RENDER.equals( processingInstruction.getTarget() ) ) {
				if ( componentInCreation ) {
					output.handler.handleText( modelFactory.createText( processingInstruction.getContent() ) );
				}
			}
			else if ( EXISTING_COMPONENT_PROXY.equals( processingInstruction.getTarget() ) ) {
				autoCreateQueue.createProxy( processingInstruction.getContent() );
			}
			else if ( CREATED_COMPONENT_PROXY.equals( processingInstruction.getTarget() ) ) {
				WebCmsComponentModel componentCreated = autoCreateQueue.getComponentCreated( processingInstruction.getContent() );
				if ( componentCreated != null ) {
					autoCreateQueue.createProxy( componentCreated.getObjectId() );
				}
			}
			else if ( START_INSTRUCTION.equals( processingInstruction.getTarget() ) ) {
				autoCreateQueue.outputStarted( processingInstruction.getContent() );
				output = new Output();
				tree.push( output );

				componentInCreation = true;
				modelProcessingState.push( ModelProcessingState.Change.component() );
			}
			else if ( STOP_INSTRUCTION.equals( processingInstruction.getTarget() ) ) {
				autoCreateQueue.outputFinished( processingInstruction.getContent(), tree.pop().buffer.toString() );
				output = tree.peek();
				componentInCreation = this.output != null;
				modelProcessingState.pop();
			}
			else if ( PlaceholderTemplatePostProcessor.START_PLACEHOLDER.equals( processingInstruction.getTarget() ) && componentInCreation ) {
				output = new Output();
				tree.push( output );
				componentInCreation = true;

				next.handleProcessingInstruction( processingInstruction );
			}
			else if ( PlaceholderTemplatePostProcessor.STOP_PLACEHOLDER.equals( processingInstruction.getTarget() ) ) {
				autoCreateQueue.placeholderRendered( processingInstruction.getContent() );

				// remove placeholder
				if ( componentInCreation ) {
					tree.pop();
					output = tree.peek();
					componentInCreation = output != null;

					// write placeholder marker
					if ( componentInCreation ) {
						output.handler.handleText( modelFactory.createText( "@@wcm:placeholder(" + processingInstruction.getContent() + ")@@" ) );
					}
				}

				// forward to the PlaceholderTemplatePostProcessor
				next.handleProcessingInstruction( processingInstruction );
			}
			else {
				next.handleProcessingInstruction( processingInstruction );
			}
		}
	}
}
