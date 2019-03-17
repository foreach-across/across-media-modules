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

import com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateQueue;
import com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateTask;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
 * Captures element output that should be used as a value for a component attribute.
 *
 * @author Arne Vandamme
 * @see ComponentAttributeElementProcessor
 * @since 0.0.7
 */
final class ComponentAttributeTemplatePostProcessor implements IPostProcessor
{
	static final String START_ATTRIBUTE = "render-to-attribute-start";
	static final String STOP_ATTRIBUTE = "render-to-attribute-finish";

	@Override
	public TemplateMode getTemplateMode() {
		return TemplateMode.HTML;
	}

	@Override
	public int getPrecedence() {
		return Integer.MAX_VALUE - 3;
	}

	@Override
	public Class<? extends ITemplateHandler> getHandlerClass() {
		return TemplateHandler.class;
	}

	public static class TemplateHandler implements ITemplateHandler
	{
		private ITemplateHandler next, outputHandler;

		private boolean buildingAttribute = false;
		private AttributeValue attributeValue;
		private final ArrayDeque<AttributeValue> tree = new ArrayDeque<>();

		private WebCmsComponentAutoCreateQueue autoCreateQueue;

		private int level = 0;

		@Override
		public void setNext( ITemplateHandler next ) {
			this.next = next;
			this.outputHandler = next;
		}

		@Override
		public void setContext( ITemplateContext context ) {
			ApplicationContext appCtx = RequestContextUtils.findWebApplicationContext( ( (WebEngineContext) context ).getRequest() );
			autoCreateQueue = appCtx.getBean( WebCmsComponentAutoCreateQueue.class );
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
			if ( START_ATTRIBUTE.equals( processingInstruction.getTarget() ) /*&& parsing()*/ ) {
				this.attributeValue = AttributeValue.from( processingInstruction.getContent() );
				tree.push( this.attributeValue );

				buildingAttribute = true;
				next = attributeValue.handler;
			}
			else if ( STOP_ATTRIBUTE.equals( processingInstruction.getTarget() ) /*&& parsing()*/ ) {
				AttributeValue attributeValue = tree.pop();
				applyAttributeValue( autoCreateQueue, attributeValue );

				this.attributeValue = tree.peek();
				buildingAttribute = this.attributeValue != null;
				next = buildingAttribute ? attributeValue.handler : outputHandler;
			}
			else {
				next.handleProcessingInstruction( processingInstruction );
			}
		}

		private void applyAttributeValue( WebCmsComponentAutoCreateQueue autoCreateQueue, AttributeValue attributeValue ) {
			WebCmsComponentAutoCreateTask currentTask = autoCreateQueue.getCurrentTask();
			if ( currentTask != null ) {
				currentTask.addAttributeValue( attributeValue.attributeType, attributeValue.attributeName, attributeValue.buffer.toString() );
			}
		}

		private boolean parsing() {
			return level > 0;
		}

		@RequiredArgsConstructor
		private static class AttributeValue
		{
			private final WebCmsComponentAutoCreateTask.Attribute attributeType;
			private final String attributeName;
			private final StringWriter buffer = new StringWriter( 1024 );
			private final ITemplateHandler handler = new OutputTemplateHandler( buffer );

			public static AttributeValue from( String typeAndName ) {
				int splitPos = StringUtils.indexOf( typeAndName, ":" );
				return new AttributeValue( WebCmsComponentAutoCreateTask.Attribute.valueOf( typeAndName.substring( 0, splitPos ) ),
				                           typeAndName.substring( splitPos + 1 ) );
			}
		}
	}
}
