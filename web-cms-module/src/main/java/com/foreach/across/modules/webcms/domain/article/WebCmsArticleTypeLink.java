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

package com.foreach.across.modules.webcms.domain.article;

import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierLink;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Date;

import static com.foreach.across.modules.webcms.domain.article.WebCmsArticle.OBJECT_TYPE;

/**
 * Link mapping to an {@link WebCmsArticleType}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@NotThreadSafe
@Entity
@DiscriminatorValue(OBJECT_TYPE)
@NoArgsConstructor
@Getter
@Setter
public class WebCmsArticleTypeLink extends WebCmsTypeSpecifierLink<WebCmsArticleTypeLink, WebCmsArticleType>
{
	@Builder(toBuilder = true)
	protected WebCmsArticleTypeLink( @Builder.ObtainVia(method = "getId") Long id,
	                                 @Builder.ObtainVia(method = "getNewEntityId") Long newEntityId,
	                                 @Builder.ObtainVia(method = "getCreatedBy") String createdBy,
	                                 @Builder.ObtainVia(method = "getCreatedDate") Date createdDate,
	                                 @Builder.ObtainVia(method = "getLastModifiedBy") String lastModifiedBy,
	                                 @Builder.ObtainVia(method = "getLastModifiedDate") Date lastModifiedDate,
	                                 @Builder.ObtainVia(method = "getOwnerObjectId") String ownerObjectId,
	                                 @Builder.ObtainVia(method = "getTypeSpecifier") WebCmsArticleType articleType,
	                                 @Builder.ObtainVia(method = "getLinkType") String linkType,
	                                 @Builder.ObtainVia(method = "getSortIndex") int sortIndex ) {
		super( id, newEntityId, createdBy, createdDate, lastModifiedBy, lastModifiedDate, ownerObjectId, articleType, linkType, sortIndex );
	}
}
