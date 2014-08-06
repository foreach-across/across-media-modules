package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.AbstractIntegrationTest;
import com.foreach.imageserver.core.business.Context;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.assertEquals;

public class ContextRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ContextRepository contextRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void getById() {
	    int fixedId = -100;
	    Context context = new Context();
	    context.setId( fixedId );
	    context.setCode( "the_application_code" );
	    contextRepository.create( context );

        Context contextFromDb = contextRepository.getById(fixedId);

        assertEquals(fixedId, contextFromDb.getId());
        assertEquals("the_application_code", contextFromDb.getCode());
    }

    @Test
    public void getByCode() {
	    int fixedId = -101;
	    Context context = new Context();
	    context.setId( fixedId );
	    context.setCode( "the_application_code_by_code" );
	    contextRepository.create( context );

	    Context contextFromDb = contextRepository.getByCode("the_application_code_by_code");

	    assertEquals(fixedId, contextFromDb.getId());
	    assertEquals("the_application_code_by_code", contextFromDb.getCode());
    }

}
