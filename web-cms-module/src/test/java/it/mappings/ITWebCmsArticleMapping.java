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

package it.mappings;

import it.AbstractCmsApplicationWithTestDataIT;
import org.junit.Test;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
public class ITWebCmsArticleMapping extends AbstractCmsApplicationWithTestDataIT
{
	@Test
	public void publicationTypeMapping() {
		getAndExpect( "/facts/fact-one-small-music", "publicationTypeMapping: Fact One: Small Music" );
	}

	@Test
	public void articleTypeMapping() {
		getAndExpect( "/facts/fact-three-big-music", "bigArticleTypeMapping: Fact Three: Big Music" );
	}

	@Test
	public void publicationMapping() {
		getAndExpect( "/facts/fact-four-big-movies", "publicationMapping: Fact Four: Big Movies" );
	}

	@Test
	public void articleTypeOnPublicationMapping() {
		getAndExpect( "/facts/fact-two-small-movies", "articleTypeOnPublicationMapping: Fact Two: Small Movies" );
	}

	@Test
	public void objectIdTrumpsAll() {
		getAndExpect( "/facts/fact-five-small-movies", "articleObjectIdMapping: Fact Five: Small Movies" );
	}
}
