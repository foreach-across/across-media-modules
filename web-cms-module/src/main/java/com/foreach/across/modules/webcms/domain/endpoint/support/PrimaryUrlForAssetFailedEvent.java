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

package com.foreach.across.modules.webcms.domain.endpoint.support;

import com.foreach.across.core.events.AcrossEvent;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.foreach.across.modules.webcms.infrastructure.ModificationReport;
import com.foreach.across.modules.webcms.infrastructure.ModificationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Event that is published by {@link com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpointService#updateOrCreatePrimaryUrlForAsset(String, WebCmsAsset, boolean)}
 * in case the automatic updating of a primary url fails.  Any handler can update the {@link #setModificationReport(ModificationReport)} to indicate
 * fall-back action has been taken.
 * <p/>
 * The consumer can rely that the initial event will return {@code true} on {@link #isFailed()}.  If the modification report has been
 * updated by a handler, this might no longer be the case.  The final modification report is the one that will be returned by the
 * the {@link com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpointService#updateOrCreatePrimaryUrlForAsset(String, WebCmsAsset, boolean)}
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Getter
@Setter
@AllArgsConstructor
public final class PrimaryUrlForAssetFailedEvent implements AcrossEvent
{
	private final WebCmsAsset asset;
	private final WebCmsAssetEndpoint endpoint;

	@NonNull
	private ModificationReport<EndpointModificationType, WebCmsUrl> modificationReport;

	/**
	 * @return true if the modification report has failure status
	 */
	public boolean isFailed() {
		return modificationReport.getModificationStatus() == ModificationStatus.FAILED;
	}
}
