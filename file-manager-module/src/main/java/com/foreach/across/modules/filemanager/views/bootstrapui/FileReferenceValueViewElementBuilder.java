package com.foreach.across.modules.filemanager.views.bootstrapui;

import com.foreach.across.modules.bootstrapui.elements.LinkViewElement;
import com.foreach.across.modules.bootstrapui.elements.StaticFormElement;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import com.foreach.across.modules.filemanager.business.reference.FileReference;
import com.foreach.across.modules.filemanager.utils.FileReferenceUtils;
import com.foreach.across.modules.web.ui.*;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilderSupport;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.foreach.across.modules.bootstrapui.ui.factories.BootstrapViewElements.bootstrap;
import static com.foreach.across.modules.web.ui.elements.HtmlViewElements.html;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
public class FileReferenceValueViewElementBuilder extends ViewElementBuilderSupport
{
	private final boolean listValue;

	public FileReferenceValueViewElementBuilder( ViewElementMode viewElementMode ) {
		listValue = ViewElementMode.LIST_VALUE.equals( viewElementMode.forSingle() );
	}

	@Override
	protected MutableViewElement createElement( ViewElementBuilderContext builderContext ) {
		List<ViewElementBuilder> links = buildLinks( EntityViewElementUtils.currentPropertyValue( builderContext ), builderContext );

		ContainerViewElementBuilderSupport container = listValue ? html.builders.container() : html.builders.div();

		int last = links.size() - 1;
		for ( int i = 0; i < links.size(); i++ ) {
			container.add( links.get( i ) );

			if ( i < last && listValue ) {
				container.add( html.builders.text( ", " ) );
			}
		}

		return container.build( builderContext );
	}

	private List<ViewElementBuilder> buildLinks( Object value, ViewElementBuilderContext builderContext ) {
		return fileReferenceStream( value )
				.map( fr -> buildDownloadLink( builderContext, fr ) )
				.collect( Collectors.toList() );
	}

	@SuppressWarnings("unchecked")
	private Stream<FileReference> fileReferenceStream( Object value ) {
		if ( value instanceof FileReference ) {
			return Stream.of( (FileReference) value );
		}

		if ( value instanceof FileReference[] ) {
			return Arrays.stream( (FileReference[]) value );
		}

		if ( value instanceof Collection ) {
			return ( (Collection<FileReference>) value ).stream();
		}

		return Stream.empty();
	}

	private ViewElementBuilder buildDownloadLink( ViewElementBuilderContext builderContext, FileReference fileReference ) {
		return bootstrap.builders.link()
		                         .text( fileReference.getName() )
		                         .url( builderContext.buildLink( FileReferenceUtils.getDownloadUrl( fileReference ) ) )
		                         .map( this::wrapAsStaticControl );
	}

	private ViewElement wrapAsStaticControl( LinkViewElement link ) {
		if ( !listValue ) {
			StaticFormElement staticControl = new StaticFormElement();
			staticControl.addChild( link );
			return staticControl;
		}

		return link;
	}
}
