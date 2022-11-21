//package it;
//
//import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
//import com.foreach.imageserver.client.ImageRequestHashBuilder;
//import com.foreach.imageserver.client.ImageServerClient;
//import com.foreach.imageserver.core.ImageServerCoreModule;
//import com.foreach.imageserver.core.config.WebConfiguration;
//import com.foreach.imageserver.dto.ImageResolutionDto;
//import com.foreach.imageserver.dto.ImageTypeDto;
//import com.foreach.imageserver.dto.ImageVariantDto;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.context.web.WebAppConfiguration;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
///**
// * @author Arne Vandamme
// */
//@ExtendWith(SpringExtension.class)
//@DirtiesContext
//@WebAppConfiguration
//@TestPropertySource(properties = "imageServerCore.md5HashToken=test")
//@ContextConfiguration(classes = ITLocalImageServerClient.Config.class)
//public class ITImageServerCoreModuleMD5AndClient
//{
//	@Autowired
//	private AcrossContextBeanRegistry beanRegistry;
//
//	@Autowired
//	private ImageServerClient imageServerClient;
//
//	@Test
//	public void imageRequestHashBuilderShouldBeCreated() {
//		assertNotNull( beanRegistry.getBeanFromModule( ImageServerCoreModule.NAME,
//		                                               WebConfiguration.IMAGE_REQUEST_HASH_BUILDER ) );
//	}
//
//	@Test
//	public void localClientShouldHaveHashBuilder() {
//		ImageRequestHashBuilder hashBuilder = beanRegistry.getBeanFromModule( ImageServerCoreModule.NAME,
//		                                                                      WebConfiguration.IMAGE_REQUEST_HASH_BUILDER );
//		String hash = hashBuilder.calculateHash( "ONLINE",
//		                                         null,
//		                                         new ImageResolutionDto( 1000, 2000 ),
//		                                         new ImageVariantDto( ImageTypeDto.TIFF ) );
//		assertEquals( "e71131abb6817f79b17e8e39078b4255", hash );
//
//		String url = imageServerClient.imageUrl( "10", "ONLINE", 1000, 2000, ImageTypeDto.TIFF );
//		assertEquals(
//				"http://somehost/img/view?iid=10&context=ONLINE&width=1000&height=2000&imageType=TIFF&hash=e71131abb6817f79b17e8e39078b4255",
//				url );
//	}
//}
