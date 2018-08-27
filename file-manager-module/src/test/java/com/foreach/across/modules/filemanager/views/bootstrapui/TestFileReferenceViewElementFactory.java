package com.foreach.across.modules.filemanager.views.bootstrapui;

import com.foreach.across.modules.entity.registry.properties.SimpleEntityPropertyDescriptor;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderSupport;
import org.junit.Test;

import static com.foreach.across.modules.filemanager.views.bootstrapui.FileReferenceViewElementFactory.FILE_REFERENCE_CONTROL;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
public class TestFileReferenceViewElementFactory
{
	private FileReferenceViewElementFactory factory = new FileReferenceViewElementFactory();

	@Test
	public void supports() {
		assertThat( factory.supports( FILE_REFERENCE_CONTROL ) ).isTrue();
	}

	@Test
	public void createsFileReferenceViewElementBuilder() {
		ViewElementBuilder builder = factory.createBuilder( new SimpleEntityPropertyDescriptor( "my-descriptor" ), ViewElementMode.CONTROL,
		                                                    FILE_REFERENCE_CONTROL );
		assertThat( builder )
				.isInstanceOf( FileReferenceViewElementBuilder.class );
		assertThat( ( (ViewElementBuilderSupport) builder ) ).extracting( "postProcessors" )
		                                                     .isNotEmpty();
		// TODO fix checking by type
//		                                                     .hasAtLeastOneElementOfType( EntityPropertyControlNamePostProcessor.class );

	}
}
