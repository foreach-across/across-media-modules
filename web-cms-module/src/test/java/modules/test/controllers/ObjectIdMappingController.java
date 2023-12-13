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

package modules.test.controllers;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.web.WebCmsAssetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Maps on specific object ids.  Also requires a request mapping specified in order to
 * be more specific than DefaultAssetMappingController.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Controller
@ResponseBody
@RequestMapping({ "/mappings/**", "/news/**" })
@WebCmsAssetMapping(
		objectId = { "wcm:asset:page:mappings-two", "wcm:asset:page:mappings-three", "wcm:asset:article:mappings-article-two" }
)
public class ObjectIdMappingController
{
	@WebCmsAssetMapping
	public String controllerObjectIdMapping( WebCmsAsset asset ) {
		return "controllerObjectIdMapping: " + asset.getName();
	}

	@WebCmsAssetMapping(objectId = "wcm:asset:page:mappings-three")
	public String methodObjectIdMapping( WebCmsAsset asset ) {
		return "methodObjectIdMapping: " + asset.getName();
	}
}
