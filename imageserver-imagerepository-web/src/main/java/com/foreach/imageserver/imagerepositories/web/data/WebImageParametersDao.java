package com.foreach.imageserver.imagerepositories.web.data;

import com.foreach.imageserver.imagerepositories.web.business.WebImageParameters;
import org.springframework.stereotype.Repository;

@Repository
public interface WebImageParametersDao {
    WebImageParameters getById(int id);

    void insert(WebImageParameters parameters);
}
