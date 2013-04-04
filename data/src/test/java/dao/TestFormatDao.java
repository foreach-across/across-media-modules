package dao;

import com.foreach.imageserver.business.image.Dimensions;
import com.foreach.imageserver.business.image.Format;
import com.foreach.imageserver.business.math.Fraction;
import com.foreach.imageserver.business.taxonomy.Application;
import com.foreach.imageserver.business.taxonomy.Group;
import com.foreach.imageserver.dao.ApplicationDao;
import com.foreach.imageserver.dao.FormatDao;
import com.foreach.imageserver.dao.GroupDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestFormatDao extends AbstractDaoTest
{
    @Autowired
    private FormatDao formatDao;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private GroupDao groupDao;

    private Group group;

    private Application app;

    @Before
    public void setup()
    {
        app = new Application();
        app.setName( "TestFormatDao.app" );
        applicationDao.insertApplication( app );

        group = new Group();
        group.setApplicationId( app.getId() );
        group.setName( "TestFormatDao.group" );
        groupDao.insertGroup( group );
    }

    @After
    public void teardown()
    {
        groupDao.deleteGroup( group.getId() );
        applicationDao.deleteApplication( app.getId() );
    }

    @Test
    public void formatCrud()
    {
        Format format = createFormat();
        formatDao.insertFormat(format);

        int formatId = format.getId();

        assertTrue( formatId > 0 );

        Format fetched = formatDao.getFormatById(formatId);
        compareFormats(format, fetched);

        format = modifyFormat( fetched );
        formatDao.updateFormat( format );
        fetched = formatDao.getFormatById( formatId );
        compareFormats( format, fetched );

        formatDao.deleteFormat(formatId);

        fetched = formatDao.getFormatById(formatId);
        assertEquals( true, fetched == null );
    }

    private Format createFormat()
    {
        Format format = new Format();
        format.setGroupId( group.getId() );
        format.setName( "TestFormatDao.format" );
        Dimensions dimensions = new Dimensions();
        format.setDimensions( dimensions );
        return format;
    }

    private Format modifyFormat( Format format )
    {
        format.setName( "TestFormatDao.format.alter" );
        Dimensions dimensions = format.getDimensions();
        dimensions.setWidth( 400 );
        dimensions.setHeight( 300 );
        dimensions.setRatio( new Fraction( 17, 23 ) );
        return format;
    }

    private void compareFormats( Format left, Format right )
    {
        assertEquals( left.getId(), right.getId() );
        assertEquals( left.getName(), right.getName() );
        assertEquals( left.getDimensions().getWidth(), right.getDimensions().getWidth() );
        assertEquals( left.getDimensions().getHeight(), right.getDimensions().getHeight() );
        assertEquals( left.getDimensions().getRatio(), right.getDimensions().getRatio() );
    }


    @Test
    @Ignore
    public void testGetFormatsByGroupId()
    {
        List<Format> formats = formatDao.getFormatsByGroupId(1);

        assertTrue(formats.size() > 0);

        Format format = formats.get(1);

        assertEquals(2L, format.getId());
        assertEquals(1, format.getGroupId());
        assertEquals(170, format.getDimensions().getWidth());
        assertEquals(120, format.getDimensions().getHeight());
        assertEquals(new Fraction( 3, 2 ), format.getDimensions().getRatio());
        assertEquals("halfImage", format.getName());
    }
}
