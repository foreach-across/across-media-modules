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

import com.foreach.across.modules.hibernate.business.EntityWithDto;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import lombok.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Getter
@Setter
public class ImageWebCmsComponentModel extends WebCmsComponentModel
{
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder(toBuilder = true)
	public static class Metadata implements EntityWithDto<Metadata>
	{
		private WebCmsImage image;

		@Override
		public Metadata toDto() {
			return toBuilder().build();
		}
	}

	@Deprecated
	private String imageServerKey;

	@Deprecated
	private String imageUrl;

	public ImageWebCmsComponentModel() {
		setMetadata( new Metadata() );
	}

	public ImageWebCmsComponentModel( WebCmsComponent component ) {
		super( component );
		setMetadata( new Metadata() );
	}

	protected ImageWebCmsComponentModel( ImageWebCmsComponentModel template ) {
		super( template );
	}

	public String getImageServerKey() {
		if ( imageServerKey != null ) {
			return imageServerKey;
		}

		return getImage() != null ? getImage().getExternalId() : null;
	}

	@Override
	public boolean isEmpty() {
		return !hasImageServerKey();
	}

	public boolean hasImageServerKey() {
		return imageServerKey != null || getImage() != null;
	}

	public boolean isExternalImage() {
		return !hasImageServerKey() && imageUrl != null;
	}

	public boolean hasImage() {
		return getImage() != null;
	}

	public WebCmsImage getImage() {
		return getMetadata( Metadata.class ).getImage();
	}

	public void setImage( WebCmsImage image ) {
		getMetadata( Metadata.class ).setImage( image );
	}

	@Override
	public ImageWebCmsComponentModel asComponentTemplate() {
		ImageWebCmsComponentModel template = new ImageWebCmsComponentModel( this );
		template.imageServerKey = imageServerKey;
		template.imageUrl = imageUrl;

		return template;
	}
}
