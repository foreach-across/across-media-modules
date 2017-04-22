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

package com.foreach.across.modules.webcms.domain.image.component;

import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.model.WebComponentModel;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Getter
@Setter
@NoArgsConstructor
public class ImageWebComponentModel extends WebComponentModel
{
	private WebCmsImage image;

	private String imageServerKey;

	private String imageUrl;

	public ImageWebComponentModel( WebCmsComponent component ) {
		super( component );
	}

	public String getImageServerKey() {
		if ( imageServerKey != null ) {
			return imageServerKey;
		}

		return image != null ? image.getExternalId() : null;
	}

	public boolean hasImageServerKey() {
		return imageServerKey != null || image != null;
	}

	public boolean isExternalImage() {
		return !hasImageServerKey() && imageUrl != null;
	}

	public boolean hasImage() {
		return image != null;
	}
}
