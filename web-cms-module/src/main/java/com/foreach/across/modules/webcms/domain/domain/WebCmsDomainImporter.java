/*
 * Copyright 2017 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.modules.webcms.domain.domain;

import com.foreach.across.modules.webcms.data.AbstractWebCmsDataImporter;
import com.foreach.across.modules.webcms.data.WebCmsDataAction;
import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import com.foreach.across.modules.webcms.domain.domain.validators.WebCmsDomainValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Map;

/**
 * Creates one (or many) @{@link WebCmsDomain}s from a yml file
 *
 * @author Steven Gentens
 * @since 0.0.3
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebCmsDomainImporter extends AbstractWebCmsDataImporter<WebCmsDomain, WebCmsDomain>
{
	private final WebCmsDomainRepository domainRepository;
	private final WebCmsDomainValidator domainValidator;

	@Override
	public boolean supports( WebCmsDataEntry data ) {
		return "domains".equals( data.getParentKey() );
	}

	@Override
	protected WebCmsDomain retrieveExistingInstance( WebCmsDataEntry data ) {
		String domainKey = data.getMapData().containsKey( "domainKey" ) ? (String) data.getMapData().get( "domainKey" ) : data.getKey();
		String objectId = (String) data.getMapData().get( "objectId" );

		WebCmsDomain existing = null;

		if ( objectId != null ) {
			existing = domainRepository.findOneByObjectId( objectId ).orElse( null );
		}

		return existing != null ? existing : domainRepository.findOneByDomainKey( domainKey ).orElse( null );
	}

	@Override
	protected WebCmsDomain createDto( WebCmsDataEntry data, WebCmsDomain existing, WebCmsDataAction action, Map<String, Object> dataValues ) {
		if ( existing == null ) {
			return createNewDomainDto( data );
		}
		else if ( action == WebCmsDataAction.REPLACE ) {
			WebCmsDomain dto = createNewDomainDto( data );
			dto.setId( existing.getId() );
			dto.setObjectId( existing.getObjectId() );
			return dto;
		}

		return existing.toDto();
	}

	private WebCmsDomain createNewDomainDto( WebCmsDataEntry data ) {
		String dataKey = data.getMapData().containsKey( "domainKey" ) ? (String) data.getMapData().get( "domainKey" ) : data.getKey();
		return WebCmsDomain.builder().domainKey( dataKey ).build();
	}

	@Override
	protected void deleteInstance( WebCmsDomain instance, WebCmsDataEntry data ) {
		domainRepository.delete( instance );
	}

	@Override
	protected void saveDto( WebCmsDomain dto, WebCmsDataAction action, WebCmsDataEntry data ) {
		LOG.debug( "Saving WebCmsDomain {} with objectId {} (insert: {}) - {}",
		           dto.getClass().getSimpleName(), dto.getObjectId(), dto.isNew(), dto );
		domainRepository.save( dto );
	}

	@Override
	protected void validate( WebCmsDomain itemToBeSaved, Errors errors ) {
		domainValidator.validate( itemToBeSaved, errors );
	}
}
