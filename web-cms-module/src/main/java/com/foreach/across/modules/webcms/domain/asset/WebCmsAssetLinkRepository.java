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

package com.foreach.across.modules.webcms.domain.asset;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.hibernate.jpa.repositories.IdBasedEntityJpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Optional;

/**
 * @author Steven Gentens
 * @since 0.0.3
 */
@Exposed
public interface WebCmsAssetLinkRepository extends IdBasedEntityJpaRepository<WebCmsAssetLink>, QuerydslPredicateExecutor<WebCmsAssetLink>
{
	List<WebCmsAssetLink> findAllByOwnerObjectId( String ownerObjectId );

	List<WebCmsAssetLink> findAllByOwnerObjectIdAndLinkTypeOrderBySortIndexAsc( String ownerObjectId, String linkType );

	Optional<WebCmsAssetLink> findOneByOwnerObjectIdAndLinkTypeAndAsset( String ownerObjectId, String linkType, WebCmsAsset asset );
}
