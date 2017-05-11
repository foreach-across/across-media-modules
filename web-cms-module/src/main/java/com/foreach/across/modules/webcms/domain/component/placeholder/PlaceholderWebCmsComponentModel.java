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

package com.foreach.across.modules.webcms.domain.component.placeholder;

import com.foreach.across.modules.hibernate.business.EntityWithDto;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
public class PlaceholderWebCmsComponentModel extends WebCmsComponentModel
{
	public static final String TYPE = "placeholder";

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder(toBuilder = true)
	public static class Metadata implements EntityWithDto<PlaceholderWebCmsComponentModel.Metadata>
	{
		@Length(max = 255)
		private String placeholderName;

		@Override
		public Metadata toDto() {
			return new Metadata( placeholderName );
		}
	}

	public PlaceholderWebCmsComponentModel() {
		setMetadata( new Metadata() );
	}

	public PlaceholderWebCmsComponentModel( WebCmsComponent component ) {
		super( component );
		setMetadata( new Metadata() );
	}

	protected PlaceholderWebCmsComponentModel( PlaceholderWebCmsComponentModel template ) {
		super( template );
	}

	@Override
	public Metadata getMetadata() {
		return super.getMetadata( Metadata.class );
	}

	public String getPlaceholderName() {
		return getMetadata().getPlaceholderName();
	}

	public void setPlaceholderName( String placeholderName ) {
		getMetadata().setPlaceholderName( placeholderName );
	}

	@Override
	public PlaceholderWebCmsComponentModel asComponentTemplate() {
		return new PlaceholderWebCmsComponentModel( this );
	}

	@Override
	public boolean isEmpty() {
		return StringUtils.isEmpty( getPlaceholderName() );
	}
}
