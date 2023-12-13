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

package com.foreach.across.modules.webcms.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 * @see ModificationType
 * @see ModificationStatus
 */
@Value
@RequiredArgsConstructor
public class ModificationReport<T extends ModificationType, U>
{
	private final T modificationType;
	private final ModificationStatus modificationStatus;
	private final U oldValue;
	private final U newValue;

	public boolean hasOldValue() {
		return oldValue != null;
	}

	public boolean hasNewValue() {
		return newValue != null;
	}
}
