package it;

import com.foreach.across.modules.filemanager.business.reference.FileReference;
import com.foreach.across.modules.filemanager.business.reference.FileReferenceRepository;
import com.foreach.across.modules.filemanager.business.reference.FileReferenceService;
import com.foreach.across.modules.filemanager.business.reference.properties.FileReferenceProperties;
import com.foreach.across.modules.filemanager.business.reference.properties.FileReferencePropertiesService;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.across.modules.hibernate.aop.EntityInterceptorAdapter;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
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
@Import(ITFileReferenceService.Config.class)
public class ITFileReferenceService extends AbstractFileManagerAndHibernateIT
{
	private static final Resource RES_TEXTFILE = new ClassPathResource( "textfile.txt" );
	private static final String EXCEPTION_DELETING_REFERENCE = "exceptionDeletingReference";

	@Autowired
	private FileReferenceService fileReferenceService;

	@Autowired
	private FileReferenceRepository fileReferenceRepository;

	@Autowired
	private FileReferencePropertiesService fileReferencePropertiesService;

	@Autowired
	private FileManager fileManager;

	@Test
	void deleteIncludingPhysicalFile() throws IOException {
		FileReference fileReference = createFileReference( "physicalDeleted" );
		checkIfObjectsExist( fileReference );
		fileReferenceService.delete( fileReference, true );
		assertThat( fileReferencePropertiesService.getProperties( fileReference.getId() ) ).isEmpty();
		assertThat( fileReferenceRepository.findById( fileReference.getId() ) ).isEmpty();
		assertThat( fileManager.exists( fileReference.getFileDescriptor() ) ).isFalse();
	}

	@Test
	void deleteExcludingPhysicalFile() throws IOException {
		FileReference fileReference = createFileReference( "physicalNotDeleted" );
		checkIfObjectsExist( fileReference );
		fileReferenceService.delete( fileReference, false );
		assertThat( fileReferencePropertiesService.getProperties( fileReference.getId() ) ).isEmpty();
		assertThat( fileReferenceRepository.findById( fileReference.getId() ) ).isEmpty();
		assertThat( fileManager.exists( fileReference.getFileDescriptor() ) ).isTrue();
	}

	@Test
	void deleteExceptionDeletingReference() throws IOException {
		FileReference fileReference = createFileReference( EXCEPTION_DELETING_REFERENCE );
		checkIfObjectsExist( fileReference );
		try {
			fileReferenceService.delete( fileReference, true );
		}
		catch ( IllegalArgumentException ignore ) {

		}
		checkIfObjectsExist( fileReference );
	}

	private FileReference createFileReference( String fileName ) throws IOException {
		MultipartFile multipartFile = new MockMultipartFile( "file", fileName + ".txt", "text/plain", RES_TEXTFILE.getInputStream() );
		FileReference fileReference = fileReferenceService.save( multipartFile, "default" );
		FileReferenceProperties properties = fileReferencePropertiesService.getProperties( fileReference.getId() );
		properties.put( "test-property", "my-property" );
		fileReferencePropertiesService.saveProperties( properties );
		return fileReference;
	}

	private void checkIfObjectsExist( FileReference fileReference ) {
		assertThat( fileReferencePropertiesService.getProperties( fileReference.getId() ) ).isNotEmpty();
		assertThat( fileReferenceRepository.findById( fileReference.getId() ) ).isNotEmpty();
		assertThat( fileManager.exists( fileReference.getFileDescriptor() ) ).isTrue();
	}

	@Configuration
	protected static class Config
	{
		@Bean
		public EntityInterceptorAdapter<FileReference> breakingFileReferenceInterceptor() {
			return new EntityInterceptorAdapter<FileReference>()
			{
				@Override
				public boolean handles( Class<?> aClass ) {
					return FileReference.class.isAssignableFrom( aClass );
				}

				@Override
				public void beforeDelete( FileReference entity ) {
					if ( StringUtils.equalsIgnoreCase( EXCEPTION_DELETING_REFERENCE + ".txt", entity.getName() ) ) {
						throw new IllegalArgumentException();
					}
				}
			};
		}
	}
}
