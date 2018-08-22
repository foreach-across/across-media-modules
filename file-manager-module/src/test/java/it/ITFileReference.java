package it;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.reference.FileReference;
import com.foreach.across.modules.filemanager.business.reference.FileReferenceRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ${AUTHOR_NAME}
 * @since ${MODULE_VERSION}
 */
public class ITFileReference extends ITFileManagerModuleIncludingOptionalModules
{
	@Autowired
	private FileReferenceRepository fileReferenceRepository;

	@Test
	public void fileDescriptorIsPersistedAsString() {
		FileDescriptor fileDescriptor = FileDescriptor.of( "test:my-file-descriptor.txt" );
		FileReference fileReference = FileReference.builder()
		                                           .fileDescriptor( fileDescriptor )
		                                           .name( "My file name" )
		                                           .build();
		FileReference saved = fileReferenceRepository.save( fileReference );
		assertThat( saved ).hasFieldOrPropertyWithValue( "uuid", fileReference.getUuid() )
		                   .hasFieldOrPropertyWithValue( "fileDescriptor", fileDescriptor )
		                   .hasFieldOrPropertyWithValue( "name", fileReference.getName() );
	}
}
