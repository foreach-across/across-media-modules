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

package com.foreach.across.modules.webcms.domain.menu;

import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.webcms.domain.WebCmsObjectSuperClass;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.*;
import java.util.Date;

/**
 * Corresponds to a named {@link com.foreach.across.modules.web.menu.Menu}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@NotThreadSafe
@Entity
@Table(name = "wcm_menu")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class WebCmsMenu extends WebCmsObjectSuperClass<WebCmsMenu>
{
	/**
	 * Prefix that all object ids of a WebCmsMenu have.
	 */
	public static final String COLLECTION_ID = "wcm:menu";

	@Id
	@GeneratedValue(generator = "seq_wcm_menu_id")
	@GenericGenerator(
			name = "seq_wcm_menu_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_wcm_menu_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "1")
			}
	)
	private Long id;

	/**
	 * Unique name of the menu within it's domain.  This should correspond to the {@link Menu} name.
	 * If the {@link WebCmsMenu} is not {@link com.foreach.across.modules.webcms.domain.domain.WebCmsDomainBound} then the name has to be unique.
	 */
	@Column
	@NotBlank
	@Length(max = 255)
	private String name;

	/**
	 * Short description of the menu, meant for UI.
	 */
	@Column
	@Length(max = 255)
	private String description;

	@Builder(toBuilder = true)
	public WebCmsMenu( @Builder.ObtainVia(method = "getId") Long id,
	                   @Builder.ObtainVia(method = "getNewEntityId") Long newEntityId,
	                   @Builder.ObtainVia(method = "getObjectId") String objectId,
	                   @Builder.ObtainVia(method = "getCreatedBy") String createdBy,
	                   @Builder.ObtainVia(method = "getCreatedDate") Date createdDate,
	                   @Builder.ObtainVia(method = "getLastModifiedBy") String lastModifiedBy,
	                   @Builder.ObtainVia(method = "getLastModifiedDate") Date lastModifiedDate,
	                   @Builder.ObtainVia(method = "getDomain") WebCmsDomain domain,
	                   String name,
	                   String description ) {
		super( id, newEntityId, objectId, createdBy, createdDate, lastModifiedBy, lastModifiedDate, domain );
		setName( name );
		setDescription( description );
	}

	@Override
	public String toString() {
		return "WebCmsMenu{" +
				"objectId='" + getObjectId() + "\'," +
				"name='" + getName() + "\'" +
				'}';
	}

	@Override
	protected String getObjectCollectionId() {
		return COLLECTION_ID;
	}
}
