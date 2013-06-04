package dao;

import com.foreach.imageserver.business.image.Crop;
import com.foreach.imageserver.business.image.Format;
import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.business.image.VariantImage;
import com.foreach.imageserver.business.Application;
import com.foreach.imageserver.business.taxonomy.Group;
import com.foreach.imageserver.dao.*;
import com.foreach.imageserver.dao.selectors.VariantImageSelector;
import com.foreach.shared.utils.SqlServerUtils;
import org.apache.commons.lang.time.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class TestVariantImageDao extends AbstractDaoTest {

    @Autowired
    private VariantImageDao variantImageDao;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private CropDao cropDao;

    @Autowired
    private FormatDao formatDao;

    @Autowired
    private ImageDao imageDao;

    @Autowired
    private GroupDao groupDao;

    private Application app;
    private ServableImageData image;
    private Format format;
    private Group group;
    private Crop crop;
    private int width, height, version;
    private Date date;

    @Before
    public void setup() {
        app = new Application();
        app.setName( "TestVariantImageDao.app" );
        applicationDao.insertApplication(app);

        group = new Group();
        group.setApplicationId(app.getId());
        group.setName( "TestVariantImageDao.group" );
        groupDao.insertGroup(group);

        image = new ServableImageData();
        image.setOriginalFileName( "TestVariantImageDao.image" );
        image.setApplicationId(app.getId());
        image.setGroupId(group.getId());
        imageDao.insertImage(image);

        format = new Format();
        format.setName("TestVariantImageDao.format");
        format.setGroupId(group.getId());
        formatDao.insertFormat(format);

        crop = new Crop();
        crop.setImageId(image.getId());
        cropDao.insertCrop(crop);

        width = 400;
        height = 300;
        version = 0;

        date = new Date();

    }

    @After
    public void teardown() {
        formatDao.deleteFormat(format.getId());
        cropDao.deleteCrop(crop.getId());
        imageDao.deleteImage(image.getId());
        groupDao.deleteGroup(group.getId());
        applicationDao.deleteApplication(app.getId());
    }

    @Test
    public void crud() {
        VariantImage variantImage = createVariantImage();
        variantImageDao.insertVariantImage(variantImage);
        assertTrue(variantImage.getId() > 0);

        VariantImage fetched = variantImageDao.getVariantImageById(variantImage.getId());
        compareVariantImages(variantImage, fetched);

        variantImage = modifyVariantImage(fetched);
        variantImageDao.updateVariantImage(variantImage);

        fetched = variantImageDao.getVariantImageById(variantImage.getId());
        compareVariantImages(variantImage, fetched);

        variantImageDao.deleteVariantImage(variantImage);

        fetched = variantImageDao.getVariantImageById(variantImage.getId());
        assertNull(fetched);
    }

    @Test
    public void updateVariantWithAlternateKey() {
        VariantImage variantImage = createVariantImage();
        variantImageDao.insertVariantImage(variantImage);

        long generatedId = variantImage.getId();

        //perform update with no primary key
        variantImage.setId(0);
        variantImage.setLastCalled(new Date(100000));
        variantImageDao.updateVariantImage(variantImage);

        VariantImage fetched = variantImageDao.getVariantImageById(generatedId);

        assertTrue(SqlServerUtils.datesCloseEnough(variantImage.getLastCalled(), fetched.getLastCalled(), 5));

        variantImageDao.deleteVariantImage(fetched);
    }

    @Test
    public void updateLastCalledDate() {
        VariantImage variantImage = createVariantImage();
        variantImageDao.insertVariantImage(variantImage);

        long generatedId = variantImage.getId();

        //perform update with no primary key
        variantImage.setId(0);
        variantImage.setLastCalled(new Date(100000));
        variantImageDao.updateVariantImageDate(variantImage);

        VariantImage fetched = variantImageDao.getVariantImageById(generatedId);

        assertTrue(SqlServerUtils.datesCloseEnough(variantImage.getLastCalled(), fetched.getLastCalled(), 5));

        variantImageDao.deleteVariantImage(fetched);
    }

    @Test
    public void deleteVariantWithAlternateKey() {
        VariantImage variantImage = createVariantImage();
        variantImageDao.insertVariantImage(variantImage);

        long generatedId = variantImage.getId();

        //perform delete with no primary key
        variantImage.setId(0);
        variantImage.setLastCalled(new Date(100000));
        variantImageDao.deleteVariantImage(variantImage);

        VariantImage fetched = variantImageDao.getVariantImageById(generatedId);

        assertNull(fetched);
    }

    @Test
    public void getVariantsForImageTest() {
        VariantImage variantImage = createVariantImage();
        variantImageDao.insertVariantImage(variantImage);

        List<VariantImage> variantImages = new ArrayList<VariantImage>();

        variantImages = variantImageDao.getAllVariantsForImage(image.getId());

        assertTrue(variantImages.size() > 0);
        assertTrue(variantImages.get(0).getImageId() == image.getId());

        variantImageDao.deleteVariantImage(variantImage);
    }

    @Test
    public void getVariantWithSelector() {
        VariantImage variantImage = createVariantImage();

        variantImageDao.insertVariantImage(variantImage);

        VariantImageSelector selector = VariantImageSelector.onImageIdAndWidthAndHeightAndVersion(image.getId(), width, height, version);

        VariantImage fetched = variantImageDao.getVariantImage(selector);
        compareVariantImages(variantImage, fetched);

        variantImageDao.deleteVariantImage(variantImage);
    }

    @Test
    public void getVariantImagesWithSelector() {
        VariantImage variantImage = createVariantImage();
        variantImageDao.insertVariantImage(variantImage);
        assertTrue(variantImage.getId() > 0);

        Date startLogDate = DateUtils.addMinutes(new Date(), -1);

        VariantImageSelector selector = VariantImageSelector.onCalledAfterThisDate(startLogDate) ;

        List<VariantImage> fetchedImages = variantImageDao.getVariantImages(selector);

        assertTrue(isLastCalledAfterDate(fetchedImages, variantImage, startLogDate));

        variantImageDao.deleteVariantImage(variantImage);

        VariantImage fetched = variantImageDao.getVariantImageById(variantImage.getId());
        assertNull(fetched);
    }

    @Test
    public void deleteVariantImagesTest() {
        VariantImage variantImage = createVariantImage();
        variantImageDao.insertVariantImage(variantImage);
        assertTrue(variantImage.getId() > 0);

        VariantImageSelector selector = VariantImageSelector.onFormatId(format.getId());

        variantImageDao.deleteVariantImages(selector);

        VariantImage fetched = variantImageDao.getVariantImageById(variantImage.getId());
        assertNull(fetched);
    }

    private boolean isLastCalledAfterDate(List<VariantImage> variantImages, VariantImage variantImage, Date date){

        for(VariantImage aVariantImage : variantImages){
            if ( aVariantImage.getId() == variantImage.getId()){
                return aVariantImage.getLastCalled().after(date);
            }
        }
        return false;
    }

    private VariantImage createVariantImage() {
        VariantImage variantImage = new VariantImage();
        variantImage.setImageId(image.getId());
        variantImage.setFormatId(format.getId());
        variantImage.setCropId(crop.getId());
        variantImage.setWidth(width);
        variantImage.setHeight(height);
        variantImage.setVersion(version);
        variantImage.setLastCalled(date);
        return variantImage;
    }

    private VariantImage modifyVariantImage(VariantImage variantImage) {
        variantImage.setWidth(600);
        variantImage.setHeight(400);
        variantImage.setVersion(1);
        variantImage.setLastCalled(new Date());
        return variantImage;
    }

    private void compareVariantImages(VariantImage left, VariantImage right) {
        assertEquals(left.getId(), right.getId());
        assertEquals(left.getImageId(), right.getImageId());
        assertEquals(left.getFormatId(), right.getFormatId());
        assertEquals(left.getCropId(), right.getCropId());
        assertEquals(left.getWidth(), right.getWidth());
        assertEquals(left.getHeight(), right.getHeight());
        assertEquals(left.getVersion(), right.getVersion());
        assertTrue(SqlServerUtils.datesCloseEnough(left.getLastCalled(), right.getLastCalled(), 5));
    }
}
