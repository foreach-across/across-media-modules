package dao;

import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.dao.ImageDao;
import com.foreach.imageserver.dao.selectors.ImageSelector;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestImageDao extends AbstractDaoTest
{
    @Autowired
    private ImageDao imageDao;

    private int applicationId = 1;
    private int groupId = 1;
    private int width = 300;
    private int height = 200;
    private long fileSize = 46080L;
    private String path = "/var/tmp/foo";
    private String originalFileName = "image";
    private String extension = "png";
    private boolean deleted = false;

    @Test
    public void crud()
    {
        ServableImageData image = createImage();
        imageDao.insertImage(image);
        assertTrue( image.getId() > 0 );

        ServableImageData fetched = imageDao.getImageById( image.getId() );
        compareImages( image, fetched );

        image = modifyImage( fetched );
        imageDao.updateImage( image );
        fetched = imageDao.getImageById( image.getId() );
        compareImages( image, fetched );

        imageDao.deleteImage( image.getId() );
    }

    private ServableImageData createImage()
    {
        ServableImageData image = new ServableImageData();
        image.setApplicationId( applicationId );
        image.setGroupId( groupId );
        image.setWidth( width );
        image.setHeight( height );
        image.setFileSize( fileSize );
        image.setPath( path );
        image.setOriginalFileName( originalFileName );
        image.setExtension( extension );
        image.setDeleted( deleted );
        return image;
    }

    private ServableImageData modifyImage( ServableImageData image )
    {
        image.setWidth( 2000 );
        image.setHeight( 3000 );
        image.setDeleted( !image.isDeleted() );
        image.setExtension( "svg" );
        image.setFileSize(92160L);
        image.setPath( "/dev/null" );
        return image;
    }

    private void compareImages( ServableImageData left, ServableImageData right )
    {
        assertEquals( left.getId(), right.getId() );
        assertEquals( left.getApplicationId(), right.getApplicationId() );
        assertEquals( left.getGroupId(), right.getGroupId() );
        assertEquals( left.getWidth(), right.getWidth() );
        assertEquals( left.getHeight(), right.getHeight() );
        assertEquals( left.getFileSize(), right.getFileSize() );
        assertEquals( left.getPath(), right.getPath() );
        assertEquals( left.getOriginalFileName(), right.getOriginalFileName() );
        assertEquals( left.getExtension(), right.getExtension() );
        //TODO: date
        assertEquals( left.isDeleted(), right.isDeleted() );
    }

    @Test
    @Ignore
    public void testGetAllImages()
    {
        List<ServableImageData> images = imageDao.getAllImages();

        assertTrue(images.size() > 0);
        assertTrue( images.get(0).getId() < images.get(1).getId() );
    }

    @Test
    @Ignore
    public void testGetImages()
    {
        ImageSelector selector = ImageSelector.onApplicationIdAndGroupId(applicationId, groupId);

        List<ServableImageData> images = imageDao.getImages(selector);

        assertTrue(images.size() > 0);
        assertTrue( images.get(0).getId() < images.get(1).getId() );
    }

    @Test
    @Ignore
    public void testGetImageCount()
    {
        ImageSelector selector = ImageSelector.onApplicationIdAndGroupId(applicationId, groupId);

        int count = imageDao.getImageCount(selector);

        assertTrue(count > 0);
    }
}
