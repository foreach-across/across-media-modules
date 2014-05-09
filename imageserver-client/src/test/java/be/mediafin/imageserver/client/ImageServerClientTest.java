package be.mediafin.imageserver.client;

import com.foreach.imageserver.dto.*;
import org.junit.Ignore;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

// TODO Rewrite this as a generic test against a test environment.
@Ignore
public class ImageServerClientTest {
/*
    private ImageServerClient imageServerClient = new ImageServerClientImpl("http://localhost:8080/", "azerty");

    @Test
    public void imageUrl() {
        String url = imageServerClient.imageUrl(10, ImageServerContext.ONLINE, 1000, 2000, ImageTypeDto.TIFF);
        assertEquals("http://localhost:8080/view?height=2000&imageType=TIFF&iid=10&width=1000&context=ONLINE", url);

        url = imageServerClient.imageUrl(10, ImageServerContext.ONLINE, null, 2000, ImageTypeDto.TIFF);
        assertEquals("http://localhost:8080/view?height=2000&imageType=TIFF&iid=10&context=SITE", url);

        url = imageServerClient.imageUrl(10, ImageServerContext.ONLINE, 1000, null, ImageTypeDto.TIFF);
        assertEquals("http://localhost:8080/view?imageType=TIFF&iid=10&width=1000&context=SITE", url);
    }

    @Test
    public void loadModifyRetrieve() throws Exception {

        ImageSaveResultDto saveResult = imageServerClient.loadImage(2513082);
        int imageId = saveResult.getImageId() ;

        assertTrue(imageId > 0);
        DimensionsDto dimensions = saveResult.getDimensionsDto() ;
        assertEquals(1842, dimensions.getWidth());
        assertEquals(3082, dimensions.getHeight());

        imageServerClient.registerImageModification(imageId, ImageServerContext.DIGITAL, 1024, 768, 500, 100, 1024, 768, 0, 0);

        List<ModificationStatusDto> modificationStatusList = imageServerClient.listModificationStatus(Arrays.asList(imageId, 10000001));
        assertEquals(2, modificationStatusList.size());
        assertEquals(imageId, modificationStatusList.get(0).getImageId());
        assertTrue(modificationStatusList.get(0).isModified());
        assertEquals(10000001, modificationStatusList.get(1).getImageId());
        assertFalse(modificationStatusList.get(1).isModified());

        List<ImageModificationDto> modifications = imageServerClient.listModifications(imageId, ImageServerContext.DIGITAL);
        assertEquals(1, modifications.size());
        assertEquals(1024, modifications.get(0).getResolution().getWidth().intValue());
        assertEquals(768, modifications.get(0).getResolution().getHeight().intValue());
        assertEquals(500, modifications.get(0).getCrop().getX());
        assertEquals(100, modifications.get(0).getCrop().getY());
        assertEquals(1024, modifications.get(0).getCrop().getWidth());
        assertEquals(768, modifications.get(0).getCrop().getHeight());
        assertEquals(0, modifications.get(0).getDensity().getWidth());
        assertEquals(0, modifications.get(0).getDensity().getHeight());

        Path tempFile = File.createTempFile("test", ".jpg").toPath();
        InputStream imageStream = imageServerClient.imageStream(imageId, ImageServerContext.DIGITAL, 1024, 768, ImageTypeDto.JPEG);
        Files.copy(imageStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        imageStream.close();

        Desktop.getDesktop().browse(tempFile.toUri());
    }

    @Test
    public void listAllowedResolutions() {
        List<ImageResolutionDto> resolutions = imageServerClient.listAllowedResolutions(ImageServerContext.ONLINE);
        assertEquals(28, resolutions.size());

        assertEquals(1000, resolutions.get(0).getWidth().intValue());
        assertEquals(1000, resolutions.get(0).getHeight().intValue());

        assertEquals(1000, resolutions.get(1).getWidth().intValue());
        assertNull(resolutions.get(1).getHeight());

        assertNull(resolutions.get(2).getWidth());
        assertEquals(1000, resolutions.get(2).getHeight().intValue());
    }
*/
}
