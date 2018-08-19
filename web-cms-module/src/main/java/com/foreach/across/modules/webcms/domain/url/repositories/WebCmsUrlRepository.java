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

package com.foreach.across.modules.webcms.domain.url.repositories;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.hibernate.jpa.repositories.IdBasedEntityJpaRepository;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Optional;

/**
 * @author Sander Van Loock
 * @since 0.0.1
 */
@Exposed
public interface WebCmsUrlRepository extends IdBasedEntityJpaRepository<WebCmsUrl>, QuerydslPredicateExecutor<WebCmsUrl>
{
	Optional<WebCmsUrl> findOneByPathAndEndpoint_Domain( String path, WebCmsDomain domain );

	List<WebCmsUrl> findAllByEndpoint( WebCmsEndpoint entity );

	Page<WebCmsUrl> findAllByEndpoint( WebCmsEndpoint entity, Pageable pageable );

	@Override
	List<WebCmsUrl> findAll( Predicate predicate );

	@Override
	List<WebCmsUrl> findAll( Predicate predicate, Sort sort );

	@Override
	List<WebCmsUrl> findAll( Predicate predicate, OrderSpecifier<?>[] orders );

	@Override
	List<WebCmsUrl> findAll( OrderSpecifier<?>[] orders );
}
