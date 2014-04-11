package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.business.Context;
import org.springframework.stereotype.Repository;

@Repository
public interface ContextDao {
    Context getById(int id);

    Context getByCode(String code);
}
