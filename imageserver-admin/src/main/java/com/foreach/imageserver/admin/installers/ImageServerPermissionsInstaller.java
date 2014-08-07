package com.foreach.imageserver.admin.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.across.modules.user.business.PermissionGroup;
import com.foreach.across.modules.user.business.Role;
import com.foreach.across.modules.user.services.PermissionService;
import com.foreach.across.modules.user.services.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

@Installer(
		description = "Define ImageServer administration permissions, assigns them to ROLE_ADMIN and ROLE_IMAGESERVER_ADMIN.",
		phase = InstallerPhase.AfterModuleBootstrap)
public class ImageServerPermissionsInstaller
{
	private static final Logger LOG = LoggerFactory.getLogger( ImageServerPermissionsInstaller.class );

	@Autowired
	private RoleService roleService;

	@Autowired
	private PermissionService permissionService;

	@InstallerMethod
	public void install() {
		createPermissionGroupAndPermissions();
		assignPermissionsToExistingRole();
	}

	private void createPermissionGroupAndPermissions() {
		permissionService.definePermission( "imageserver view images",
		                                    "The user can view images and access the imageserver administrative interface.",
		                                    "imageserver" );
		permissionService.definePermission( "imageserver upload images", "The user can upload images.", "imageserver" );
		permissionService.definePermission( "imageserver manage resolutions",
		                                    "The user can modify and create image resolutions.", "imageserver" );

		PermissionGroup permissionGroup = permissionService.getPermissionGroup( "imageserver" );
		permissionGroup.setTitle( "Module: ImageServer administration" );
		permissionGroup.setDescription(
				"Permissions for managing the ImageServer entities." );

		permissionService.save( permissionGroup );
	}

	private void assignPermissionsToExistingRole() {
		// Extend the admin role with the new permissions
		Role role = roleService.getRole( "ROLE_ADMIN" );

		if ( role != null ) {
			role.addPermission( "imageserver view images", "imageserver upload images",
			                    "imageserver manage resolutions" );
			roleService.save( role );
		}
		else {
			LOG.warn(
					"ROLE_ADMIN does not appear to exist - the ImageServer permissions have not been assigned to any role." );
		}

		// Create a separate role for managing image server
		roleService.defineRole(
				"ROLE_IMAGESERVER_ADMIN",
				"ImageServer administrator",
				Arrays.asList( "imageserver view images", "imageserver upload images",
				               "imageserver manage resolutions" )
		);
	}
}

