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

package com.foreach.across.modules.webcms;

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainBound;

/**
 * Set of EntityModule related attributes.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
public interface WebCmsEntityAttributes
{
	/**
	 * Attribute on {@link com.foreach.across.modules.entity.registry.EntityConfiguration} that specifies if
	 * an entity should be manageable per domain.  This will overrule the default domain-bound or not-domain-bound behaviour.
	 *
	 * @see com.foreach.across.modules.webcms.domain.domain.support.CurrentDomainAwareAllowableActionsBuilder
	 */
	String ALLOW_PER_DOMAIN = "webCmsModule.multiDomain.allowPerDomain";

	/**
	 * Attribute that contains the {@link String} with the name of the property that links to the {@link WebCmsDomain}.
	 * By default only {@link WebCmsDomainBound} entities support multi-domain,
	 * where the implicit value of this attribute would then be <em>domain</em>.
	 * <p/>
	 * The presence of this attribute would activate multi-domain auto-configuration for entities not implementing {@link WebCmsDomainBound} directly.
	 * Examples include {@link com.foreach.across.modules.webcms.domain.url.WebCmsUrl} and {@link com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItem}
	 * that have their domain linked transitively (resp. via <em>endpoint.domain</em> and <em>menu.domain</em>).
	 */
	String DOMAIN_PROPERTY = "webCmsModule.multiDomain.domainProperty";

	/**
	 * Describes the set of attributes that are registered on an entity configuration
	 * Manually registering these will skip the auto-configuration for that segment.
	 */
	interface MultiDomainConfiguration
	{
		/**
		 * Set to true on any {@link com.foreach.across.modules.entity.registry.EntityConfiguration}
		 * or {@link com.foreach.across.modules.entity.registry.EntityAssociation} to indicate the list view has already been adjusted for multi-domain support.
		 */
		String LIST_VIEW_ADJUSTED = "webCmsModule.multiDomain.configuration.listViewAdjusted";

		/**
		 * Set to true on any {@link com.foreach.across.modules.entity.registry.EntityConfiguration} to indicate that the
		 * {@link com.foreach.across.modules.entity.registry.EntityModel} has been modified for multi-domain support.
		 */
		String ENTITY_MODEL_ADJUSTED = "webCmsModule.multiDomain.configuration.entityModelAdjusted";

		/**
		 * Set to true on any {@link com.foreach.across.modules.entity.registry.EntityConfiguration} to indicate that the
		 * {@link com.foreach.across.modules.entity.actions.EntityConfigurationAllowableActionsBuilder} has been modified for multi-domain-support.
		 */
		String ALLOWABLE_ACTIONS_ADJUSTED = "webCmsModule.multiDomain.configuration.allowableActionsAdjusted";

		/**
		 * Set to true on any {@link com.foreach.across.modules.entity.registry.EntityConfiguration} or
		 * {@link com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor} to indicate that
		 * the {@link com.foreach.across.modules.entity.EntityAttributes#OPTIONS_ENTITY_QUERY} has been modified for multi-domain support.
		 */
		String OPTIONS_QUERY_ADJUSTED = "webCmsModule.multiDomain.configuration.optionsQueryAdjusted";

		/**
		 * Set to true if auto-configuration of multi-domain support has already been fully performed.
		 * When manually set, the entire entity processing will be skipped.
		 */
		String FINISHED = "webCmsModule.multiDomain.configuration.finished";
	}

}
