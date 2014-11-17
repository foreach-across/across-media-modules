package com.foreach.across.modules.spring.batch;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossRole;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.filters.BeanFilterComposite;
import com.foreach.across.core.filters.ClassBeanFilter;
import com.foreach.across.core.transformers.PrimaryBeanTransformer;
import com.foreach.across.modules.spring.batch.installers.SpringBatchSchemaInstaller;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;

import java.util.Arrays;

/**
 * @author Arne Vandamme
 */
@AcrossRole(AcrossModuleRole.INFRASTRUCTURE)
public class SpringBatchModule extends AcrossModule
{
	public static final String NAME = "SpringBatchModule";

	public SpringBatchModule() {
		setExposeFilter(
				new BeanFilterComposite(
						getExposeFilter(),
						new ClassBeanFilter(
								JobRepository.class,
								JobLauncher.class,
								JobRegistry.class,
								StepBuilderFactory.class,
								JobBuilderFactory.class,
						        JobExplorer.class
						)
				)
		);

		setExposeTransformer(
				new PrimaryBeanTransformer( Arrays.asList( "jobLauncher" ) )
		);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return "Services for enabling Spring data jobs in modules.";
	}

	@Override
	public Object[] getInstallers() {
		return new Object[] { SpringBatchSchemaInstaller.class };
	}
}
