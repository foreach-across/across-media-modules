package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.business.WebOriginalImage;
import org.springframework.stereotype.Repository;

@Repository
public interface WebOriginalImageDao {
    WebOriginalImage getById(int id);

    WebOriginalImage getByParameters(String url);

    void insert(WebOriginalImage image);
}
