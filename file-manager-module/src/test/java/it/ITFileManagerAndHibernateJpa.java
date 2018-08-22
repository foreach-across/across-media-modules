package it;

import com.foreach.across.modules.filemanager.business.reference.FileReferenceRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
public class ITFileManagerAndHibernateJpa extends AbstractFileManagerAndHibernateIT
{
	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void repositoryIsCreated() {
		assertThat( applicationContext.getParent().getBeansOfType( FileReferenceRepository.class ) ).isNotEmpty();
	}
}
