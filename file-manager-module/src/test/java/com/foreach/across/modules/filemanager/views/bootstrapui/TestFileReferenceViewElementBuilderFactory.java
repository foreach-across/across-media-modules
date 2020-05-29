package com.foreach.across.modules.filemanager.views.bootstrapui;

import com.foreach.across.modules.entity.registry.properties.SimpleEntityPropertyDescriptor;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderSupport;
import org.junit.jupiter.api.Test;

import static com.foreach.across.modules.filemanager.views.bootstrapui.FileReferenceViewElementBuilderFactory.FILE_REFERENCE_CONTROL;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
class TestFileReferenceViewElementBuilderFactory
{
	private FileReferenceViewElementBuilderFactory factory = new FileReferenceViewElementBuilderFactory();

	@Test
	void supports() {
		assertThat( factory.supports( FILE_REFERENCE_CONTROL ) ).isTrue();
	}

	@Test
	void createsFileReferenceViewElementBuilder() {
		ViewElementBuilder builder = factory.createBuilder( new SimpleEntityPropertyDescriptor( "my-descriptor" ), ViewElementMode.CONTROL,
		                                                    FILE_REFERENCE_CONTROL );
		assertThat( builder )
				.isInstanceOf( FileReferenceControlViewElementBuilder.class );
		assertThat( ( (ViewElementBuilderSupport) builder ) ).extracting( "postProcessors" ).asList().hasSize( 1 );
		// TODO fix checking by type
		// .hasAtLeastOneElementOfType( EntityPropertyControlNamePostProcessor.class );

	}
}
