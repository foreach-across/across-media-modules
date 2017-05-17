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

package com.foreach.across.modules.webcms.domain.component.proxy;

import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import lombok.Getter;

/**
 * Represents a component that renders as a different component.
 * This component is considered a <em>proxy</em> of the target component.
 * <p/>
 * As a component can only have a single owner, the strategy for containers is to create
 * proxy components instead of linking to the same component multiple times.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
public class ProxyWebCmsComponentModel extends WebCmsComponentModel
{
	public static final String TYPE = "proxy";

	@Getter
	private WebCmsComponentModel target;

	public ProxyWebCmsComponentModel() {
		super();
	}

	public ProxyWebCmsComponentModel( WebCmsComponent component,
	                                  WebCmsComponentModel target ) {
		super( component );
		this.target = target;
	}

	/**
	 * Target component model that is proxied by this component.
	 * Not allowed to be another proxy.
	 */
	public void setTarget( WebCmsComponentModel target ) {
		if ( target instanceof ProxyWebCmsComponentModel ) {
			throw new IllegalArgumentException( "Not allowed to set another proxy as target for a proxy" );
		}
		this.target = target;
	}

	@Override
	public ProxyWebCmsComponentModel asComponentTemplate() {
		return new ProxyWebCmsComponentModel( getComponent().asTemplate(), target );
	}

	/**
	 * @return true if there is no target or the target is empty
	 * @see #hasTarget()
	 */
	@Override
	public boolean isEmpty() {
		return target == null || target.isEmpty();
	}

	/**
	 * @return true if a target is attached to this proxy
	 */
	public boolean hasTarget() {
		return target != null;
	}
}
