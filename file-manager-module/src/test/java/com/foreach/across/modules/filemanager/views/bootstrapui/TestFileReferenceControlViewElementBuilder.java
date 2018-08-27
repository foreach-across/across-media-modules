package com.foreach.across.modules.filemanager.views.bootstrapui;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.bootstrapui.BootstrapUiModule;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.filemanager.business.reference.FileReference;
import com.foreach.across.modules.web.ui.DefaultViewElementBuilderContext;
import com.foreach.across.test.support.AbstractViewElementTemplateTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
@ContextConfiguration
public class TestFileReferenceControlViewElementBuilder extends AbstractViewElementTemplateTest
{
	private DefaultViewElementBuilderContext builderContext;
	private FileReferenceControlViewElementBuilder builder;
	private EntityPropertyDescriptor descriptor;
	private Object entity;

	private final static String UUID = "some-uuid";
	private final static String PROPERTY_NAME = "my-property";
	private final static String FILE_NAME = "my-file.txt";
	private final static String TEMPLATE = "<script type='text/html' data-role='selected-item-template'>" +
			"<div>replaceByName<a role='button' href='#' class='remove-file btn btn-link'>" +
			"<span aria-hidden='true' class='glyphicon glyphicon-remove'></span></a></div></script>";
	private final static String PRE_SELECTED = "<div class='js-file-reference-control'>" + TEMPLATE +
			"<input type='file' name='" + PROPERTY_NAME + "' id='" + PROPERTY_NAME + "' class='hidden' />" +
			"<div><a href='/reference/" + UUID + "'>" + FILE_NAME + "</a><a role='button' href='#' class='remove-file btn btn-link'>" +
			"<span aria-hidden='true' class='glyphicon glyphicon-remove'></span></a></div>" + "</div>";
	private final static String NOT_SELECTED = "<div class='js-file-reference-control'>" + TEMPLATE +
			"<input type='file' name='" + PROPERTY_NAME + "' id='" + PROPERTY_NAME + "' /></div>";

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {
		builder = new FileReferenceControlViewElementBuilder();

		descriptor = mock( EntityPropertyDescriptor.class );
		when( descriptor.getPropertyType() ).thenReturn( (Class) FileReference.class );
		when( descriptor.getName() ).thenReturn( PROPERTY_NAME );
		entity = new Object();

		builderContext = new DefaultViewElementBuilderContext();
		builderContext.setAttribute( EntityPropertyDescriptor.class, descriptor );
		builderContext.setAttribute( "entity", entity );
	}

	@Test
	public void fileReferenceDoesNotExist() {
		when( descriptor.getPropertyValue( entity ) ).thenReturn( null );
		renderAndExpect( builder.createElement( builderContext ), NOT_SELECTED );
	}

	@Test
	public void fileReferenceExists() {
		when( descriptor.getPropertyValue( entity ) ).thenReturn( FileReference.builder().name( FILE_NAME ).uuid( UUID ).build() );
		renderAndExpect( builder.createElement( builderContext ), PRE_SELECTED );
	}

	@Configuration
	protected static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new BootstrapUiModule() );
		}
	}
}
