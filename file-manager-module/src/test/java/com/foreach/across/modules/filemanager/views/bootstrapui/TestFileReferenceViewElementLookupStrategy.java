package com.foreach.across.modules.filemanager.views.bootstrapui;

import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.filemanager.business.reference.FileReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.convert.TypeDescriptor;

import java.util.List;

import static com.foreach.across.modules.filemanager.views.bootstrapui.FileReferenceViewElementBuilderFactory.FILE_REFERENCE_CONTROL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestFileReferenceViewElementLookupStrategy
{
	private FileReferenceViewElementLookupStrategy strategy = new FileReferenceViewElementLookupStrategy();

	private EntityPropertyDescriptor singleValueDescriptor = mock( EntityPropertyDescriptor.class );
	private EntityPropertyDescriptor multiValueDescriptor = mock( EntityPropertyDescriptor.class );

	@Before
	public void setUp() {
		reset( singleValueDescriptor );
	}

	@Test
	public void supportsControlAndValueModes() {
		assertThat( lookup( FileReference.class, ViewElementMode.FORM_READ ) ).isNull();
		assertThat( lookup( FileReference.class, ViewElementMode.FORM_WRITE ) ).isNull();
		assertThat( lookup( FileReference.class, ViewElementMode.LABEL ) ).isNull();
		assertThat( lookup( FileReference.class, ViewElementMode.LIST_LABEL ) ).isNull();
		assertThat( lookup( FileReference.class, ViewElementMode.FILTER_CONTROL ) ).isNull();
		assertThat( lookup( FileReference.class, ViewElementMode.CONTROL ) ).isEqualTo( FILE_REFERENCE_CONTROL );
		assertThat( lookup( FileReference.class, ViewElementMode.LIST_CONTROL ) ).isEqualTo( FILE_REFERENCE_CONTROL );
		assertThat( lookup( FileReference.class, ViewElementMode.LIST_VALUE ) ).isEqualTo( FILE_REFERENCE_CONTROL );
		assertThat( lookup( FileReference.class, ViewElementMode.VALUE ) ).isEqualTo( FILE_REFERENCE_CONTROL );

		assertThat( lookup( FileReference.class, ViewElementMode.CONTROL.forMultiple() ) ).isEqualTo( FILE_REFERENCE_CONTROL );
		assertThat( lookup( FileReference.class, ViewElementMode.FILTER_CONTROL.forMultiple() ) ).isNull();
		assertThat( lookup( FileReference.class, ViewElementMode.VALUE.forMultiple() ) ).isEqualTo( FILE_REFERENCE_CONTROL );
		assertThat( lookup( FileReference.class, ViewElementMode.LABEL.forMultiple() ) ).isNull();
	}

	@Test
	public void onlySupportsFileReferenceTypes() {
		assertThat( lookup( String.class, ViewElementMode.CONTROL ) ).isNull();
		assertThat( lookup( String.class, ViewElementMode.LIST_CONTROL ) ).isNull();
		assertThat( lookup( (Class) null, ViewElementMode.CONTROL ) ).isNull();
		assertThat( lookup( (Class) null, ViewElementMode.VALUE ) ).isNull();

		assertThat( lookup( FileReference.class, ViewElementMode.CONTROL ) ).isNotNull();
		assertThat( lookup( FileReference.class, ViewElementMode.VALUE ) ).isNotNull();

		assertThat( lookupMulti( FileReference.class, ViewElementMode.CONTROL ) ).isNotNull();
		assertThat( lookupMulti( FileReference.class, ViewElementMode.VALUE ) ).isNotNull();
		assertThat( lookupMulti( FileReference.class, ViewElementMode.LIST_LABEL ) ).isNull();
		assertThat( lookupMulti( FileReference.class, ViewElementMode.FORM_WRITE ) ).isNull();
		assertThat( lookupMulti( String.class, ViewElementMode.LIST_LABEL ) ).isNull();
		assertThat( lookupMulti( null, ViewElementMode.FORM_WRITE ) ).isNull();
	}

	@SuppressWarnings("unchecked")
	private String lookup( Class propertyType, ViewElementMode viewElementMode ) {
		TypeDescriptor typeDescriptor = TypeDescriptor.valueOf( propertyType );
		when( singleValueDescriptor.getPropertyTypeDescriptor() ).thenReturn( typeDescriptor );
		return lookup( singleValueDescriptor, viewElementMode );
	}

	@SuppressWarnings("unchecked")
	private String lookupMulti( Class propertyType, ViewElementMode viewElementMode ) {
		TypeDescriptor typeDescriptor = TypeDescriptor.valueOf( propertyType );
		when( multiValueDescriptor.getPropertyTypeDescriptor() ).thenReturn( TypeDescriptor.collection( List.class, typeDescriptor ) );
		return lookup( multiValueDescriptor, viewElementMode );
	}

	private String lookup( EntityPropertyDescriptor descriptor, ViewElementMode viewElementMode ) {
		return strategy.findElementType( descriptor, viewElementMode );
	}

}
