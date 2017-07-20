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

package com.foreach.across.modules.webcms.data;

import static com.foreach.across.modules.webcms.infrastructure.WebCmsUtils.convertImportActionToDataAction;

public class WebCmsDataActionResolver {
	/**
	 * Resolve the import action into the actual action to perform depending if there's an
	 * existing item we're dealing with or not.  If this method returns {@code null} the
	 * processing of the data entry will be skipped.
	 *
	 * @param existing item or {@code null} if none
	 * @param data     set of data being passed
	 * @return action to perform or {@code null} if none
	 */
	public static WebCmsDataAction resolveAction( Object existing, WebCmsDataEntry data ) {
		WebCmsDataImportAction requested = data.getImportAction();
		return convertImportActionToDataAction( existing, requested );
	}

}
