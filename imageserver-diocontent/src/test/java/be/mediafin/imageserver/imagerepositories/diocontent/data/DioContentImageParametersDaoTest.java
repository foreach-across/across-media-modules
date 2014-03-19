package be.mediafin.imageserver.imagerepositories.diocontent.data;

import be.mediafin.imageserver.imagerepositories.diocontent.AbstractDioContentIntegrationTest;
import be.mediafin.imageserver.imagerepositories.diocontent.business.DioContentImageParameters;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DioContentImageParametersDaoTest extends AbstractDioContentIntegrationTest {

    @Autowired
    private DioContentImageParametersDao dioContentImageParametersDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void insertAndGetById() {
        String imageSql = "INSERT INTO IMAGE ( imageId, created, repositoryCode ) VALUES ( ?, ?, ? )";
        jdbcTemplate.update(imageSql, 121212, new Date(2012, 11, 13), "the_repository_code");

        DioContentImageParameters writtenParameters = new DioContentImageParameters();
        writtenParameters.setImageId(121212);
        writtenParameters.setDioContentId(666);
        writtenParameters.setDimensions(dimensions(123, 321));
        writtenParameters.setImageType(ImageType.TIFF);

        dioContentImageParametersDao.insert(writtenParameters);

        DioContentImageParameters readParameters = dioContentImageParametersDao.getById(121212);
        assertNotNull(readParameters);
        assertEquals(writtenParameters.getImageId(), readParameters.getImageId());
        assertEquals(writtenParameters.getDioContentId(), readParameters.getDioContentId());
        assertEquals(writtenParameters.getDimensions().getWidth(), readParameters.getDimensions().getWidth());
        assertEquals(writtenParameters.getDimensions().getHeight(), readParameters.getDimensions().getHeight());
        assertEquals(writtenParameters.getImageType(), readParameters.getImageType());
    }

    private Dimensions dimensions(int width, int height) {
        Dimensions dimensions = new Dimensions();
        dimensions.setWidth(width);
        dimensions.setHeight(height);
        return dimensions;
    }

}
