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

import com.foreach.across.modules.hibernate.business.SettableIdBasedEntity;
import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import com.foreach.across.modules.web.menu.Menu;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.*;

/**
 * Corresponds to a named {@link com.foreach.across.modules.web.menu.Menu}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@NotThreadSafe
@Entity
@Table(name = "wcm_menu")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class WebCmsMenu extends SettableIdBasedEntity<WebCmsMenu>
{
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
	 * Unique name of the menu.  This should correspond to the {@link Menu} name.
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
}
