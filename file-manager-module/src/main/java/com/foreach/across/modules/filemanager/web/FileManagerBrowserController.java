package com.foreach.across.modules.filemanager.web;

import com.foreach.across.core.annotations.ConditionalOnDevelopmentMode;
import com.foreach.across.modules.adminweb.annotations.AdminWebController;
import com.foreach.across.modules.adminweb.menu.AdminMenu;
import com.foreach.across.modules.adminweb.menu.AdminMenuEvent;
import com.foreach.across.modules.adminweb.ui.PageContentStructure;
import com.foreach.across.modules.filemanager.business.*;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.across.modules.filemanager.services.FileRepository;
import com.foreach.across.modules.filemanager.services.FileRepositoryRegistry;
import com.foreach.across.modules.web.ui.elements.TemplateViewElement;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Development mode controller which allows browsing all registered file repositories.
 *
 * @author Arne Vandamme
 * @since 1.4.0
 */
@SuppressWarnings("unused")
@AdminWebController
@RequestMapping("/ax/developer/fileManagerModule/fileManager")
@ConditionalOnDevelopmentMode
@RequiredArgsConstructor
class FileManagerBrowserController
{
	private final PageContentStructure page;
	private final FileRepositoryRegistry fileRepositoryRegistry;
	private final FileManager fileManager;

	@EventListener
	public void registerAdminMenu( AdminMenuEvent menuEvent ) {
		menuEvent.builder().item( "/ax/developer/fileManagerModule/fileManager", "File repositories" );
	}

	@GetMapping
	public String listFileRepositories( Model model ) {
		model.addAttribute( "repositories", fileRepositoryRegistry.listRepositories() );

		page.setPageTitle( "File repositories" );
		page.addChild( new TemplateViewElement( "th/FileManagerModule/dev/repository-browser :: listAllRepositories(${repositories})" ) );

		return PageContentStructure.TEMPLATE;
	}

	@GetMapping(params = { "repositoryId" })
	public String listResources( @RequestParam String repositoryId,
	                             @RequestParam(required = false, name = "folder") FolderDescriptor folderDescriptor,
	                             Model model,
	                             AdminMenu adminMenu ) {
		FileRepository repository = fileRepositoryRegistry.getRepository( repositoryId );
		model.addAttribute( "fileRepository", repository );

		FolderResource folder = folderDescriptor != null ? repository.getFolderResource( folderDescriptor ) : repository.getRootFolderResource();
		model.addAttribute(
				"fileRepositoryResources",
				folder.listResources( false ).stream().map( Resource::from ).sorted().collect( Collectors.toList() )
		);

		adminMenu.breadcrumbLeaf( repository.getRepositoryId() );

		LinkedList<FolderResource> breadCrumb = new LinkedList<>();
		breadCrumb.add( folder );

		Optional<FolderResource> parent = folder.getParentFolderResource();
		while ( parent.isPresent() ) {
			FolderResource f = parent.get();
			breadCrumb.addFirst( f );
			parent = f.getParentFolderResource();
		}

		model.addAttribute( "folderResourcePath", breadCrumb );

		page.setPageTitle( "File repositories: " + repository.getRepositoryId() );
		page.addChild(
				new TemplateViewElement(
						"th/FileManagerModule/dev/repository-browser :: listResources(${fileRepository},${folderResourcePath},${fileRepositoryResources})" )
		);

		return PageContentStructure.TEMPLATE;
	}

	@GetMapping("/{action:download|view}")
	@SneakyThrows
	public ResponseEntity<org.springframework.core.io.Resource> downloadFile( @RequestParam(name = "file") FileDescriptor fileDescriptor,
	                                                                          @PathVariable String action ) {
		FileResource file = fileManager.getFileResource( fileDescriptor );

		HttpHeaders headers = new HttpHeaders();
		if ( "download".equals( action ) ) {
			headers.set( HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getFilename() );
			headers.setContentLength( file.contentLength() );
		}
		else {
			String contentType = URLConnection.guessContentTypeFromName( file.getFilename() );
			headers.set( HttpHeaders.CONTENT_TYPE, contentType );
		}

		return new ResponseEntity<>( file, headers, HttpStatus.OK );
	}

	@Data
	@Builder
	static class Resource implements Comparable<Resource>
	{
		private String name;
		private boolean folder;
		private long contentLength;
		private Date lastModified;
		private FileRepositoryResourceDescriptor descriptor;

		public String readableFileSize() {
			if ( contentLength <= 0 ) {
				return "0";
			}
			final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
			int digitGroups = (int) ( Math.log10( contentLength ) / Math.log10( 1024 ) );
			return new DecimalFormat( "#,##0.#" ).format( contentLength / Math.pow( 1024, digitGroups ) ) + " " + units[digitGroups];
		}

		@Override
		public int compareTo( Resource o ) {
			int i = Boolean.compare( o.folder, folder );

			if ( i == 0 ) {
				i = name.compareToIgnoreCase( o.name );
			}

			return i;
		}

		@SneakyThrows
		static Resource from( FileRepositoryResource resource ) {
			if ( resource instanceof FolderResource ) {
				FolderResource folder = (FolderResource) resource;
				return Resource.builder().name( folder.getFolderName() ).folder( true ).descriptor( folder.getDescriptor() ).build();
			}

			FileResource fr = (FileResource) resource;
			return Resource.builder().name( fr.getFilename() ).descriptor( fr.getDescriptor() ).contentLength( fr.contentLength() )
			               .lastModified( fr.lastModified() > 0 ? new Date( fr.lastModified() ) : null )
			               .build();
		}
	}
}
