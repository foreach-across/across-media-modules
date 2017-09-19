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

package test.component.model;

import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.webcms.WebCmsModule;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.component.model.AbstractWebCmsComponentModelReader;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.across.test.AcrossWebAppConfiguration;
import it.DynamicDataSourceConfigurer;
import lombok.Data;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@AcrossWebAppConfiguration
public class TestAbstractWebCmsComponentModelReader
{
	@Autowired
	private MyReader reader;

	private WebCmsComponent component;
	private WebCmsComponentType componentType;

	@Before
	public void setUp() throws Exception {
		componentType = new WebCmsComponentType();
		component = new WebCmsComponent();
		component.setComponentType( componentType );
	}

	@Test
	public void noMetadataSpecifiedOnComponentType() {
		assertNull( reader.metadata( null, component ) );

		One one = new One();
		assertSame( one, reader.metadata( one, component ) );
	}

	@Test
	public void newBlankInstanceCreatedIfComponentTypeSpecifies() {
		componentType.getAttributes().put( "metadata", Two.class.getName() );

		assertTrue( Two.class.isInstance( reader.metadata( null, component ) ) );
		assertTrue( Two.class.isInstance( reader.metadata( new One(), component ) ) );

		Two two = new Two();
		assertSame( two, reader.metadata( two, component ) );
	}

	@Test
	public void currentMetadataReturnedIfConstructionOfOtherFails() {
		componentType.getAttributes().put( "metadata", Three.class.getName() );

		assertNull( reader.metadata( null, component ) );
		Two two = new Two();
		assertSame( two, reader.metadata( two, component ) );
	}

	@Test
	public void buildNewMetadataFromJson() {
		componentType.getAttributes().put( "metadata", One.class.getName() );
		component.setMetadata( "{\"title\": \"my title\", \"count\": 123}" );

		One one = (One) reader.metadata( null, component );
		assertEquals( "my title", one.title );
		assertEquals( 123, one.count );

		one = (One) reader.metadata( new Two(), component );
		assertEquals( "my title", one.title );
		assertEquals( 123, one.count );
	}

	@Test
	public void currentMetadataIsUpdatedFromJson() {
		componentType.getAttributes().put( "metadata", One.class.getName() );
		component.setMetadata( "{\"title\": \"my title\"}" );

		One current = new One();
		current.setTitle( "some title" );
		current.setCount( 999 );

		One one = (One) reader.metadata( current, component );
		assertSame( current, one );
		assertEquals( "my title", one.title );
		assertEquals( 999, one.count );

		component.setMetadata( "{\"count\": 123}" );

		one = (One) reader.metadata( current, component );
		assertSame( current, one );
		assertEquals( "my title", one.title );
		assertEquals( 123, one.count );
	}

	@Test
	public void currentMetadataReturnedIfDeserializationFails() {
		componentType.getAttributes().put( "metadata", One.class.getName() );
		component.setMetadata( "dsfqsdfqsdfqs" );

		One current = new One();
		current.setTitle( "some title" );
		current.setCount( 999 );

		One one = (One) reader.metadata( current, component );
		assertSame( current, one );
		assertEquals( "some title", one.title );
		assertEquals( 999, one.count );
	}

	@Test
	public void deserializationIgnoresUnknownProperties() {
		componentType.getAttributes().put( "metadata", One.class.getName() );
		component.setMetadata( "{\"subTitle\": \"my title\", \"count\": 123}" );

		One current = new One();
		current.setTitle( "some title" );
		current.setCount( 999 );

		One one = (One) reader.metadata( current, component );
		assertEquals( "some title", one.title );
		assertEquals( 123, one.count );
	}

	@Test
	public void deserializationOfPropertyCanReturnPartialUpdate() {
		componentType.getAttributes().put( "metadata", One.class.getName() );
		component.setMetadata( "{\"title\": \"my title\", \"count\": \"15619-456-abc\"}" );

		One current = new One();
		current.setTitle( "some title" );
		current.setCount( 999 );

		One one = (One) reader.metadata( current, component );
		assertEquals( "my title", one.title );
		assertEquals( 999, one.count );
	}

	@AcrossTestConfiguration(modules = WebCmsModule.NAME)
	protected static class Config extends DynamicDataSourceConfigurer
	{
		@Bean
		public EmptyAcrossModule myModule() {
			return new EmptyAcrossModule( "MyModule", MyReader.class );
		}
	}

	@Data
	public static class One
	{
		private String title;
		private int count;
	}

	public static class Two
	{
	}

	public static class Three
	{
		Three( String param ) {
			throw new RuntimeException( param );
		}
	}

	@Exposed
	protected static class MyReader extends AbstractWebCmsComponentModelReader<WebCmsComponentModel>
	{
		@Override
		public boolean supports( WebCmsComponent component ) {
			return false;
		}

		@Override
		protected WebCmsComponentModel buildComponentModel( WebCmsComponent component ) {
			return null;
		}

		public Object metadata( Object currentMetadata, WebCmsComponent component ) {
			return buildMetadata( currentMetadata, component );
		}
	}
}
