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

package com.foreach.across.modules.webcms.domain.asset.component;

import com.foreach.across.modules.hibernate.business.SettableIdAuditableEntity;
import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.*;
import java.util.Date;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@NotThreadSafe
@Entity
@Table(name = "wcm_asset_component_link")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class WebCmsAssetComponentLink extends SettableIdAuditableEntity<WebCmsAssetComponentLink>
{
	@Id
	@GeneratedValue(generator = "seq_wcm_asset_comp_link_id")
	@GenericGenerator(
			name = "seq_wcm_asset_comp_link_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_wcm_asset_comp_link_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "5")
			}
	)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "asset_id")
	private WebCmsAsset asset;

	@ManyToOne
	@JoinColumn(name = "component_id")
	private WebCmsComponent component;

	//private WebCmsAssetComponentLinkType linkType;

	@Column(name = "name")
	@Length(max = 100)
	private String name;

	@Column(name = "sort_index")
	private int sortIndex;

	@Builder(toBuilder = true)
	protected WebCmsAssetComponentLink( @Builder.ObtainVia(method = "getId") Long id,
	                                    @Builder.ObtainVia(method = "getNewEntityId") Long newEntityId,
	                                    @Builder.ObtainVia(method = "getCreatedBy") String createdBy,
	                                    @Builder.ObtainVia(method = "getCreatedDate") Date createdDate,
	                                    @Builder.ObtainVia(method = "getLastModifiedBy") String lastModifiedBy,
	                                    @Builder.ObtainVia(method = "getLastModifiedDate") Date lastModifiedDate,
	                                    WebCmsAsset asset,
	                                    WebCmsComponent component,
	                                    String name,
	                                    int sortIndex ) {
		setNewEntityId( newEntityId );
		setId( id );
		setCreatedBy( createdBy );
		setCreatedDate( createdDate );
		setLastModifiedBy( lastModifiedBy );
		setLastModifiedDate( lastModifiedDate );

		setAsset( asset );
		setComponent( component );
		setName( name );
		setSortIndex( sortIndex );
	}

	@Override
	public String toString() {
		return "WebCmsAssetComponentLink{" +
				"id=" + id +
				", asset=" + asset +
				", component=" + component +
				", name='" + name + '\'' +
				", sortIndex=" + sortIndex +
				'}';
	}
}
