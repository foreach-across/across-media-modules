package com.foreach.across.modules.filemanager.web;

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.modules.filemanager.business.reference.FileReference;
import com.foreach.across.modules.filemanager.business.reference.FileReferenceRepository;
import com.foreach.across.modules.filemanager.business.reference.QFileReference;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.web.AcrossWebModule;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Provides an API to serve {@link FileReference}s.
 *
 * @author Steven Gentens
 * @since 1.3.0
 */
@Controller
@RequiredArgsConstructor
@ConditionalOnAcrossModule(allOf = { AcrossHibernateJpaModule.NAME, AcrossWebModule.NAME })
public class FileReferenceController
{
	public static final String BASE_PATH = "/api/fmm/reference";
	private final FileReferenceRepository fileReferenceRepository;
	private final FileManager fileManager;

	@GetMapping(BASE_PATH + "/{uuid}")
	public ResponseEntity<InputStreamResource> downloadFile( @PathVariable("uuid") String uuid ) {
		FileReference fileReference = fileReferenceRepository.findOne( QFileReference.fileReference.uuid.eq( uuid ) )
		                                                     .orElseThrow( () -> new IllegalArgumentException( "No such file reference: " + uuid ) );

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType( MediaType.valueOf( fileReference.getMimeType() ) );
		headers.set( HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileReference.getName() );
		headers.setContentLength( fileReference.getFileSize() );

		return new ResponseEntity<>( new InputStreamResource( fileManager.getInputStream( fileReference.getFileDescriptor() ) ),
		                             headers,
		                             HttpStatus.OK );
	}
}
