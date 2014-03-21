package be.mediafin.imageserver.client;

import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.dto.ImageTypeDto;
import com.foreach.imageserver.dto.ModificationStatusDto;
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

    private ImageServerClient imageServerClient = new ImageServerClientImpl("http://localhost:8080/", "azerty");

    @Test
    public void imageUrl() {
        String url = imageServerClient.imageUrl(10, ImageServerContext.SITE, 1000, 2000, ImageTypeDto.TIFF);
        assertEquals("http://localhost:8080/view?height=2000&imageType=TIFF&iid=10&width=1000&context=SITE", url);

        url = imageServerClient.imageUrl(10, ImageServerContext.SITE, null, 2000, ImageTypeDto.TIFF);
        assertEquals("http://localhost:8080/view?height=2000&imageType=TIFF&iid=10&context=SITE", url);

        url = imageServerClient.imageUrl(10, ImageServerContext.SITE, 1000, null, ImageTypeDto.TIFF);
        assertEquals("http://localhost:8080/view?imageType=TIFF&iid=10&width=1000&context=SITE", url);
    }

    @Test
    public void loadModifyRetrieve() throws Exception {
        int imageId = 20;

        DimensionsDto dimensions = imageServerClient.loadImage(imageId, 2513082);
        assertEquals(1842, dimensions.getWidth());
        assertEquals(3082, dimensions.getHeight());

        imageServerClient.registerImageModification(imageId, ImageServerContext.SITE, 1000, 1000, 500, 0, 1000, 1000, 0, 0);

        List<ModificationStatusDto> modificationStatusList = imageServerClient.listModificationStatus(Arrays.asList(imageId, 10000001));
        assertEquals(2, modificationStatusList.size());
        assertEquals(imageId, modificationStatusList.get(0).getImageId());
        assertTrue(modificationStatusList.get(0).isModified());
        assertEquals(10000001, modificationStatusList.get(1).getImageId());
        assertFalse(modificationStatusList.get(1).isModified());

        Path tempFile = File.createTempFile("test", ".jpg").toPath();
        InputStream imageStream = imageServerClient.imageStream(imageId, ImageServerContext.SITE, 1000, 1000, ImageTypeDto.JPEG);
        Files.copy(imageStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        imageStream.close();

        Desktop.getDesktop().browse(tempFile.toUri());
    }

    @Test
    public void listAllowedResolutions() {
        List<ImageResolutionDto> resolutions = imageServerClient.listAllowedResolutions(ImageServerContext.SITE);
        assertEquals(3, resolutions.size());

        assertEquals(1000, resolutions.get(0).getWidth().intValue());
        assertEquals(1000, resolutions.get(0).getHeight().intValue());

        assertEquals(1000, resolutions.get(1).getWidth().intValue());
        assertNull(resolutions.get(1).getHeight());

        assertNull(resolutions.get(2).getWidth());
        assertEquals(1000, resolutions.get(2).getHeight().intValue());
    }

}
