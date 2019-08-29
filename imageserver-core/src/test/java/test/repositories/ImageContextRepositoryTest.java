package test.repositories;

import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.repositories.ImageContextRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test.AbstractIntegrationTest;

import static org.junit.Assert.assertEquals;

public class ImageContextRepositoryTest extends AbstractIntegrationTest
{

	@Autowired
	private ImageContextRepository contextRepository;

	@Test
	public void getById() {
		Long fixedId = -100L;
		ImageContext context = new ImageContext();
		context.setId( fixedId );
		context.setCode( "the_application_code" );
		contextRepository.save( context );

		ImageContext contextFromDb = contextRepository.findById( fixedId ).orElse( null );

		assertEquals( fixedId, contextFromDb.getId() );
		assertEquals( "the_application_code", contextFromDb.getCode() );
	}

	@Test
	public void getByCode() {
		Long fixedId = -101L;
		ImageContext context = new ImageContext();
		context.setId( fixedId );
		context.setCode( "the_application_code_by_code" );
		contextRepository.save( context );

		ImageContext contextFromDb = contextRepository.getByCode( "the_application_code_by_code" );

		assertEquals( fixedId, contextFromDb.getId() );
		assertEquals( "the_application_code_by_code", contextFromDb.getCode() );
	}

}
