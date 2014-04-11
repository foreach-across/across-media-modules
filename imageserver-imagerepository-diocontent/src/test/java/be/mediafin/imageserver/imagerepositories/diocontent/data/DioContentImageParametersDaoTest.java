package be.mediafin.imageserver.imagerepositories.diocontent.data;

import be.mediafin.imageserver.imagerepositories.diocontent.AbstractDioContentIntegrationTest;
import be.mediafin.imageserver.imagerepositories.diocontent.business.DioContentImageParameters;
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
        String imageSql = "INSERT INTO IMAGE ( imageId, created, repositoryCode, width, height, imageTypeId ) VALUES ( ?, ?, ?, ?, ?, ? )";
        jdbcTemplate.update(imageSql, 121212, new Date(2012, 11, 13), "the_repository_code", 222, 111, 2);

        DioContentImageParameters writtenParameters = new DioContentImageParameters();
        writtenParameters.setImageId(121212);
        writtenParameters.setDioContentId(666);

        dioContentImageParametersDao.insert(writtenParameters);

        DioContentImageParameters readParameters = dioContentImageParametersDao.getById(121212);
        assertNotNull(readParameters);
        assertEquals(writtenParameters.getImageId(), readParameters.getImageId());
        assertEquals(writtenParameters.getDioContentId(), readParameters.getDioContentId());
    }

}
