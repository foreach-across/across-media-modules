package it;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.reference.FileReference;
import com.foreach.across.modules.filemanager.business.reference.FileReferenceRepository;
import com.foreach.across.modules.filemanager.business.reference.FileReferenceService;
import com.foreach.across.modules.filemanager.business.reference.properties.FileReferenceProperties;
import com.foreach.across.modules.filemanager.business.reference.properties.FileReferencePropertiesService;
import com.foreach.across.modules.filemanager.services.FileManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
public class ITFileReferenceService extends AbstractFileManagerAndHibernateIT
{
	private static final Resource RES_TEXTFILE = new ClassPathResource( "textfile.txt" );

	@Autowired
	private FileReferenceService fileReferenceService;

	@Autowired
	private FileReferenceRepository fileReferenceRepository;

	@Autowired
	private FileReferencePropertiesService fileReferencePropertiesService;

	@Autowired
	private FileManager fileManager;

	private FileReference fileReference;

	@Before
	public void setUp() throws IOException {
		MultipartFile multipartFile = new MockMultipartFile( "file", "textfile.txt", "text/plain", RES_TEXTFILE.getInputStream() );
		fileReference = fileReferenceService.save( multipartFile );
		FileReferenceProperties properties = fileReferencePropertiesService.getProperties( fileReference.getId() );
		properties.put( "test-property", "my-property" );
		fileReferencePropertiesService.saveProperties( properties );
	}

	@After
	public void cleanUp() {
		fileReferencePropertiesService.deleteProperties( fileReference.getId() );
		fileReferenceRepository.delete( fileReference );
	}

	@Test
	public void deleteIncludingPhysicalFile() {
		checkIfObjectsExist();
		Long id = fileReference.getId();
		FileDescriptor descriptor = fileReference.getFileDescriptor();

		fileReferenceService.delete( fileReference, true );

		fileReferenceRepository.flush();
		assertThat( fileReferencePropertiesService.getProperties( id ) ).isEmpty();
		assertThat( fileReferenceRepository.findOne( id ) ).isNull();
		assertThat( fileManager.exists( descriptor ) ).isFalse();
	}

	private void checkIfObjectsExist() {
		assertThat( fileReferencePropertiesService.getProperties( fileReference.getId() ) ).isNotEmpty();
		assertThat( fileReferenceRepository.findOne( fileReference.getId() ) ).isNotNull();
		assertThat( fileManager.exists( fileReference.getFileDescriptor() ) ).isTrue();
	}
}
