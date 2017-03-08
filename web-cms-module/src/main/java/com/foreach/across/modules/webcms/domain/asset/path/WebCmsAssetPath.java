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

package com.foreach.across.modules.webcms.domain.asset.path;

/**
 * Represents a navigable path between a collection of {@link com.foreach.across.modules.webcms.domain.asset.WebCmsAsset}s.
 * A node on the path is represented by a {@link WebCmsAssetPathNode}.  The first node will have no previous item, the last
 * node will have no next.  All others should have a previous and next and the same asset can only occur once in a path.
 * <p/>
 * Can serve as a more efficient construct for previous/next calculation.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class WebCmsAssetPath
{
	private String name;
}
