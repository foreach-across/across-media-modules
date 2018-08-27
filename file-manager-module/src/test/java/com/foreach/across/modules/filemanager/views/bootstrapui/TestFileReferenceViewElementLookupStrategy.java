package com.foreach.across.modules.filemanager.views.bootstrapui;

import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.filemanager.business.reference.FileReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

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

	private EntityPropertyDescriptor descriptor = mock( EntityPropertyDescriptor.class );

	@Before
	public void setUp() {
		reset( descriptor );
	}

	@Test
	public void strategyOnlySupportsControlMode() {
		assertThat( lookup( FileReference.class, ViewElementMode.CONTROL ) )
				.isEqualTo( FileReferenceViewElementFactory.FILE_REFERENCE_CONTROL );
		assertThat( lookup( FileReference.class, ViewElementMode.FILTER_CONTROL ) ).isNull();
		assertThat( lookup( FileReference.class, ViewElementMode.FORM_READ ) ).isNull();
		assertThat( lookup( FileReference.class, ViewElementMode.FORM_WRITE ) ).isNull();
		assertThat( lookup( FileReference.class, ViewElementMode.LABEL ) ).isNull();
		assertThat( lookup( FileReference.class, ViewElementMode.LIST_CONTROL ) ).isNull();
		assertThat( lookup( FileReference.class, ViewElementMode.LIST_LABEL ) ).isNull();
		assertThat( lookup( FileReference.class, ViewElementMode.LIST_VALUE ) ).isNull();
		assertThat( lookup( FileReference.class, ViewElementMode.VALUE ) ).isNull();
	}

	@Test
	public void doesNotSupportMultiple() {
		assertThat( lookup( FileReference.class, ViewElementMode.CONTROL.forMultiple() ) ).isNull();
		assertThat( lookup( FileReference.class, ViewElementMode.VALUE.forMultiple() ) ).isNull();
	}

	@Test
	public void supportsFileReferenceProperties() {
		assertThat( lookup( null, ViewElementMode.CONTROL ) ).isNull();
		assertThat( lookup( null, ViewElementMode.VALUE ) ).isNull();
		assertThat( lookup( String.class, ViewElementMode.CONTROL ) ).isNull();
		assertThat( lookup( String.class, ViewElementMode.LIST_CONTROL ) ).isNull();
	}

	@SuppressWarnings("unchecked")
	private String lookup( Class propertyType, ViewElementMode viewElementMode ) {
		when( descriptor.getPropertyType() ).thenReturn( propertyType );
		return strategy.findElementType( descriptor, viewElementMode );
	}

}
