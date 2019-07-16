package com.foreach.bamboo.imageserver;

import com.atlassian.bamboo.specs.api.builders.BambooKey;
import com.atlassian.bamboo.specs.api.builders.Variable;
import com.atlassian.bamboo.specs.api.builders.notification.Notification;
import com.atlassian.bamboo.specs.api.builders.permission.Permissions;
import com.atlassian.bamboo.specs.api.builders.permission.PlanPermissions;
import com.atlassian.bamboo.specs.api.builders.plan.Plan;
import com.atlassian.bamboo.specs.api.builders.plan.PlanIdentifier;
import com.atlassian.bamboo.specs.api.builders.plan.branches.BranchCleanup;
import com.atlassian.bamboo.specs.api.builders.plan.branches.PlanBranchManagement;
import com.atlassian.bamboo.specs.api.builders.plan.configuration.AllOtherPluginsConfiguration;
import com.atlassian.bamboo.specs.api.builders.plan.configuration.ConcurrentBuilds;
import com.atlassian.bamboo.specs.api.builders.plan.dependencies.Dependencies;
import com.atlassian.bamboo.specs.api.builders.plan.dependencies.DependenciesConfiguration;
import com.atlassian.bamboo.specs.api.builders.project.Project;
import com.atlassian.bamboo.specs.builders.notification.PlanFailedNotification;
import com.atlassian.bamboo.specs.builders.notification.ResponsibleRecipient;
import com.atlassian.bamboo.specs.builders.trigger.RepositoryPollingTrigger;
import com.atlassian.bamboo.specs.util.BambooServer;

import static com.foreach.bamboo.imageserver.Stages.*;

/**
 * Plan configuration for Bamboo.
 * Learn more on: <a href="https://confluence.atlassian.com/display/BAMBOO/Bamboo+Specs">https://confluence.atlassian.com/display/BAMBOO/Bamboo+Specs</a>
 */
@com.atlassian.bamboo.specs.api.BambooSpec
public class BambooSpec
{

	/**
	 * Run main to publish plan on Bamboo
	 */
	public static void main( final String[] args ) {
		//By default credentials are read from the '.credentials' file.
		BambooServer bambooServer = new BambooServer( "https://bamboo.projects.foreach.be" );
		Plan plan = new BambooSpec().createPlan();
		bambooServer.publish( plan );
		PlanPermissions planPermission = new BambooSpec().createPlanPermission( plan.getIdentifier() );
		bambooServer.publish( planPermission );
	}

	PlanPermissions createPlanPermission( PlanIdentifier planIdentifier ) {
		Permissions permission = new Permissions();
		return new PlanPermissions( planIdentifier.getProjectKey(), planIdentifier.getPlanKey() ).permissions( permission );
	}

	Plan createPlan() {
		Project project = new Project().key( new BambooKey( "IMAGESERVERTEST" ) ).name( "Foreach - ImageServer" );
		return new Plan( project, "ImageServer - Build snapshot", new BambooKey( "IMAGESERVERPLATFORM10" ) )
				.description( "Build and Test ImageServer" )
				.variables( new Variable( "deployToSonatype", "false" ) )
				.pluginConfigurations( new ConcurrentBuilds().useSystemWideDefault( false ), new AllOtherPluginsConfiguration() )
				.stages( unitTests(), integrationTests(), deploySnapshot() )
				.linkedRepositories( "Across - ImageServer - develop", "Across - Bamboo Specs" )
				.triggers( new RepositoryPollingTrigger().description( "poll master branch" ) )
				.planBranchManagement( new PlanBranchManagement().createForVcsBranch().delete(
						new BranchCleanup().whenRemovedFromRepositoryAfterDays( 7 ).whenInactiveInRepositoryAfterDays( 30 ) )
				                                                 .notificationForCommitters() )
				.dependencies( new Dependencies().configuration( new DependenciesConfiguration().enabledForBranches( false ) ) )
				.notifications( new Notification().type( new PlanFailedNotification() ).recipients( new ResponsibleRecipient() ) );
	}

}
