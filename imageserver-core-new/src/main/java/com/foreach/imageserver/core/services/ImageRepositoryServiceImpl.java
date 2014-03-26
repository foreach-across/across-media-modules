package com.foreach.imageserver.core.services;

import com.foreach.across.core.annotations.Refreshable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Refreshable
public class ImageRepositoryServiceImpl implements ImageRepositoryService {

    @Autowired
    private ImageRepositoryRegistry imageRepositoryRegistry;

    @Override
    public ImageRepository determineImageRepository(String code) {
        for (ImageRepository repository : imageRepositoryRegistry.getMembers()) {
            if (repository.getCode().equals(code)) {
                return repository;
            }
        }
        return null;
    }
}
