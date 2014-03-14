package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.business.WebOriginalImageParameters;
import org.springframework.stereotype.Repository;

@Repository
public interface WebOriginalImageParametersDao {
    WebOriginalImageParameters getByParameters(String url);

    void insert(WebOriginalImageParameters imageParameters);
}
