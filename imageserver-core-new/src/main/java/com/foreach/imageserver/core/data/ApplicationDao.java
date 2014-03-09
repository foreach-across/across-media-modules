package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.business.Application;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationDao {
    Application getById(int id);
}
