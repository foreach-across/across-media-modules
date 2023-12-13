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

/**
 * Represents the different actions possible on a data item.
 * Differs between an (incremental) update and a replacement of a
 * <p/>
 * Note that action rules are represented by the {@link WebCmsDataImportAction}.
 *
 * @author Arne Vandamme
 * @see WebCmsDataImportAction
 * @since 0.0.2
 */
public enum WebCmsDataAction
{
	CREATE,
	UPDATE,     /* Partial update of an existing item */
	REPLACE,    /* Full replacement of an existing item */
	DELETE
}

