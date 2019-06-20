package com.foreach.bamboo.imageserver;

import com.atlassian.bamboo.specs.api.builders.BambooKey;
import com.atlassian.bamboo.specs.api.builders.plan.Job;
import com.atlassian.bamboo.specs.api.builders.plan.configuration.AllOtherPluginsConfiguration;
import com.atlassian.bamboo.specs.api.builders.repository.VcsRepositoryIdentifier;
import com.atlassian.bamboo.specs.api.builders.requirement.Requirement;
import com.atlassian.bamboo.specs.api.builders.task.Task;
import com.atlassian.bamboo.specs.builders.task.CheckoutItem;
import com.atlassian.bamboo.specs.builders.task.ScriptTask;
import com.atlassian.bamboo.specs.builders.task.TestParserTask;
import com.atlassian.bamboo.specs.builders.task.VcsCheckoutTask;
import com.atlassian.bamboo.specs.model.task.TestParserTaskProperties;

public class Jobs
{
	public static Job crossDbTest( String name, String key, String datasource ) {
		return new Job( name, new BambooKey( key ) )
				.pluginConfigurations( new AllOtherPluginsConfiguration() )
				.tasks(
						defaultRepositoryCheckoutTask(),
						bambooSpecsRepositoryCheckoutTask(),
                         /*
                         new MavenTask()
                                 .description("Run integration tests")
                                 .enabled(false)
                                 .goal("clean compile test-compile pre-integration-test failsafe:integration-test -DacrossTest.datasource=mysql-imageserver -Dmaven.test.failure.ignore=true")
                                 .jdk("JDK 1.8")
                                 .executableLabel("Maven 3.0")
                                 .hasTests(true),
                          */
						new ScriptTask()
								.description( name )
								.inlineBody(
										"DATASOURCE=" + datasource + " docker-compose -f docker-compose.yml -f across-bamboo-specs/docker-dbs.yml -f docker-crossdb.yml up --exit-code-from maven-gm --abort-on-container-exit maven-gm " + datasource )
				)
				.finalTasks( new TestParserTask( TestParserTaskProperties.TestType.JUNIT )
						             .description( "Parse surefire reports" )
						             .resultDirectories( "**/target/surefire-reports/*.xml" ),
				             new ScriptTask()
						             .description( "Cleanup docker" )
						             .inlineBody(
								             "DATASOURCE=" + datasource + " docker-compose -f docker-compose.yml -f across-bamboo-specs/docker-dbs.yml -f docker-crossdb.yml down --remove-orphans -v" ) )
				.requirements( requiresLinux() );
	}

	public static Requirement requiresLinux() {
		return new Requirement( "os" ).matchValue( "linux" ).matchType( Requirement.MatchType.EQUALS );
	}

	public static Task defaultRepositoryCheckoutTask() {
		return new VcsCheckoutTask().description( "Checkout Default Repository" )
		                            .checkoutItems( new CheckoutItem().defaultRepository() )
		                            .cleanCheckout( true );
	}

	public static Task bambooSpecsRepositoryCheckoutTask() {
		return new VcsCheckoutTask().description( "Checkout Bamboo Specs Repository" )
		                            .checkoutItems( new CheckoutItem().repository( new VcsRepositoryIdentifier().name( "Across - Bamboo Specs" ) )
		                                                              .path( "across-bamboo-specs" )
		                            );
	}
}
