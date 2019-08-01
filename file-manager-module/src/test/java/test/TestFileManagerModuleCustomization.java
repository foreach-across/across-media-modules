package test;

import com.foreach.across.modules.filemanager.FileManagerModule;
import com.foreach.across.modules.filemanager.services.*;
import com.foreach.across.test.AcrossTestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 1.4.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
class TestFileManagerModuleCustomization
{
	@Autowired
	private FileManager fileManager;

	@Autowired
	private FileManagerAware myRepository;

	@Test
	void fileRepositoryBeansAreAutomaticallyPickedUp() {
		FileRepositoryDelegate fileRepository = (FileRepositoryDelegate) fileManager.getRepository( "my-repository" );
		assertThat( fileRepository.getActualImplementation() ).isSameAs( myRepository );
		verify( myRepository ).setFileManager( fileManager );
	}

	@Test
	void customFileRepositoryFactoryIsUsed() {
		FileRepository createdRepository = fileManager.getRepository( "newlyCreatedRepository" );
		assertThat( createdRepository ).isNotNull();
		verify( ( (FileManagerAware) ( (FileRepositoryDelegate) createdRepository ).getActualImplementation() ) ).setFileManager( fileManager );
		assertThat( fileManager.getRepository( "newlyCreatedRepository" ) ).isSameAs( createdRepository );
	}

	@AcrossTestConfiguration
	static class RepositoriesConfiguration
	{
		@Bean
		FileManagerModule fileManagerModule() {
			return new FileManagerModule();
		}

		@Bean
		FileRepository myRepository() {
			FileRepository fileRepository = mock( FileRepository.class, withSettings().extraInterfaces( FileManagerAware.class ) );
			when( fileRepository.getRepositoryId() ).thenReturn( "my-repository" );
			return fileRepository;
		}

		@Bean
		FileRepositoryFactory fileRepositoryFactory() {
			FileRepositoryFactory factory = mock( FileRepositoryFactory.class );
			FileRepository repository = mock( FileRepository.class, withSettings().extraInterfaces( FileManagerAware.class ) );
			when( repository.getRepositoryId() ).thenReturn( UUID.randomUUID().toString() );
			when( factory.create( any() ) ).thenReturn( repository );
			return factory;
		}
	}
}
