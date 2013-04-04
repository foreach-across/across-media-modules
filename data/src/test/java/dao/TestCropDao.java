package dao;

import com.foreach.imageserver.business.geometry.Point;
import com.foreach.imageserver.business.geometry.Rect;
import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.image.Crop;
import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.business.taxonomy.Application;
import com.foreach.imageserver.business.taxonomy.Group;
import com.foreach.imageserver.dao.ApplicationDao;
import com.foreach.imageserver.dao.CropDao;
import com.foreach.imageserver.dao.GroupDao;
import com.foreach.imageserver.dao.ImageDao;
import com.foreach.imageserver.dao.selectors.CropSelector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

public class TestCropDao extends AbstractDaoTest
{
    @Autowired
    private CropDao cropDao;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private GroupDao groupDao;

    @Autowired
    private ImageDao imageDao;

    private Application app;
    private Group group;
    private ServableImageData image;

    @Before
    public void setup()
    {
        app = new Application();
        app.setName( "TestCropDao.app" );
        applicationDao.insertApplication( app );

        group = new Group();
        group.setApplicationId( app.getId() );
        group.setName( "TestCropDao.group" );
        groupDao.insertGroup( group );

        image = new ServableImageData();
        image.setApplicationId( app.getId() );
        image.setGroupId( group.getId() );
        imageDao.insertImage( image );
    }

    @After
    public void teardown()
    {
        imageDao.deleteImage( image.getId() );
        groupDao.deleteGroup( group.getId() );
        applicationDao.deleteApplication( app.getId() );
    }

    @Test
    public void cropCrud()
    {
        Crop crop = createCrop();
        cropDao.insertCrop( crop );

        long cropId = crop.getId();

        assertTrue( cropId > 0 );

        Crop fetched = cropDao.getCropById( cropId );
        compareCrops( crop, fetched );

        crop = modifyCrop( fetched );
        cropDao.updateCrop( crop );
        fetched = cropDao.getCropById( cropId );
        compareCrops( crop, fetched );

        cropDao.deleteCrop( cropId );


        fetched = cropDao.getCropById( cropId );
        assertEquals( true, fetched == null );
    }

    private Crop createCrop()
    {
        Crop crop = new Crop();
        crop.setImageId( image.getId() );
        crop.setCropRect( new Rect( new Point( 10, 20 ), new Size( 400, 300 ) ) );
        crop.setRatioHeight( 4 );
        crop.setRatioWidth( 3 );
        crop.setVersion( 1 );
        crop.setTargetWidth( 800 );
        return crop;
    }

    private Crop modifyCrop( Crop crop )
    {
        crop.setCropRect( new Rect( new Point( 40, 55 ), new Size( 200, 200 ) ) );
        crop.setRatioHeight( 1 );
        crop.setRatioWidth( 1 );
        crop.setVersion( 2 );
        crop.setTargetWidth( 500 );
        return crop;
    }


    private void compareCrops( Crop left, Crop right )
    {
        assertEquals( left.getId(), right.getId() );
        assertEquals( left.getImageId(), right.getImageId() );
        assertEquals( left.getCropRect(), right.getCropRect() );
        assertEquals( left.getRatioWidth(), right.getRatioWidth() );
        assertEquals( left.getRatioHeight(), right.getRatioHeight() );
        assertEquals( left.getVersion(), right.getVersion() );
        assertEquals( left.getTargetWidth(), right.getTargetWidth() );
    }

    @Test
    public void cropSelector()
    {
        Crop crop = createCrop();
        cropDao.insertCrop( crop );
        long cropId = crop.getId();


        CropSelector selector = CropSelector.uniqueCrop(
                crop.getImageId(), crop.getAspectRatio(), crop.getTargetWidth(), crop.getVersion() );

        List<Crop> fetched = cropDao.getCrops( selector );
        assertEquals( 1, fetched.size() );

        cropDao.deleteCrop( cropId );
    }
}
