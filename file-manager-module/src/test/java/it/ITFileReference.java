package it;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.reference.FileReference;
import com.foreach.across.modules.filemanager.business.reference.FileReferenceRepository;
import liquibase.util.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintViolationException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
public class ITFileReference extends AbstractFileManagerAndHibernateIT
{
	public static final FileDescriptor FILE_DESCRIPTOR = FileDescriptor.of( "test:my-file-descriptor.txt" );
	@Autowired
	private FileReferenceRepository fileReferenceRepository;

	@Test
	public void fileDescriptorIsPersistedAsString() {
		FileReference fileReference = FileReference.builder()
		                                           .fileDescriptor( FILE_DESCRIPTOR )
		                                           .name( "My file name" )
		                                           .build();
		FileReference saved = fileReferenceRepository.save( fileReference );
		assertThat( saved ).hasFieldOrPropertyWithValue( "uuid", fileReference.getUuid() )
		                   .hasFieldOrPropertyWithValue( "fileDescriptor", FILE_DESCRIPTOR )
		                   .hasFieldOrPropertyWithValue( "name", fileReference.getName() );
	}

	@Test(expected = ConstraintViolationException.class)
	public void validateNameLength() {
		FileReference fileReference = FileReference.builder()
		                                           .fileDescriptor( FILE_DESCRIPTOR )
		                                           .name( StringUtils.repeat( "a", 300 ) )
		                                           .build();
		fileReferenceRepository.saveAndFlush( fileReference );
	}
}
