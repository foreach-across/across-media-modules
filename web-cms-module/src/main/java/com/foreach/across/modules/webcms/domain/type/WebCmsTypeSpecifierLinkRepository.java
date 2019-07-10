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

package com.foreach.across.modules.webcms.domain.type;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.hibernate.jpa.repositories.IdBasedEntityJpaRepository;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import java.util.List;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Exposed
public interface WebCmsTypeSpecifierLinkRepository extends IdBasedEntityJpaRepository<WebCmsTypeSpecifierLink>, QueryDslPredicateExecutor<WebCmsTypeSpecifierLink>
{
	@Override
	List<WebCmsTypeSpecifierLink> findAll( Predicate predicate );

	@Override
	List<WebCmsTypeSpecifierLink> findAll( Predicate predicate, Sort sort );

	@Override
	List<WebCmsTypeSpecifierLink> findAll( Predicate predicate, OrderSpecifier<?>[] orders );

	@Override
	List<WebCmsTypeSpecifierLink> findAll( OrderSpecifier<?>[] orders );

	List<WebCmsTypeSpecifierLink> findAllByOwnerObjectId( String ownerObjectId );

	List<WebCmsTypeSpecifierLink> findAllByOwnerObjectIdAndLinkTypeOrderBySortIndexAsc( String ownerObjectId, String linkType );

	WebCmsTypeSpecifierLink findOneByOwnerObjectIdAndLinkTypeAndTypeSpecifier( String ownerObjectId, String linkType, WebCmsTypeSpecifier typeSpecifier );
}
