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

package com.foreach.across.modules.webcms.domain.endpoint.web.controllers;

import com.foreach.across.modules.web.mvc.condition.AbstractCustomRequestCondition;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Raf Ceuls
 * @since 0.0.2
 */
public abstract class AbstractAssetCondition<T extends AbstractCustomRequestCondition<T>> extends AbstractCustomRequestCondition<T>
{

	protected int compareArrays( String[] self, String[] other ) {
		if ( self.length > 0 && other.length == 0 ) {
			return -1;
		}
		else if ( self.length == 0 && other.length > 0 ) {
			return 1;
		}
		else if ( self.length > 0 || other.length > 0 ) {
			return Integer.compare( self.length, other.length );
		}
		return 0;
	}

	protected String[] combineArrays( String[] wide, String[] narrow ) {
		if ( wide.length == 0 && narrow.length == 0 ) {
			return new String[0];
		}

		if ( wide.length == 0 ) {
			return narrow;
		}

		if ( narrow.length == 0 ) {
			return wide;
		}

		List<String> combined = new ArrayList<String>( narrow.length );

		// check that "narrow" is more specific (being a subset) or equal to "wide"
		for ( String otherMember : narrow ) {
			if ( !ArrayUtils.contains( wide, otherMember ) ) {
				throw new InvalidWebCmsEndpointConditionCombination(
						String.format( "Unable to combine endpoints: method level must be same or narrower than controller: %s is not a subset of %s",
						               Arrays.toString( narrow ), Arrays.toString( wide ) )
				);
			}
			combined.add( otherMember );
		}
		return combined.toArray( new String[combined.size()] );
	}
}
