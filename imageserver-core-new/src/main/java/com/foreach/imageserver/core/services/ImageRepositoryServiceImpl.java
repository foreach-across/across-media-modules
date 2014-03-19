package com.foreach.imageserver.core.services;

import com.foreach.across.core.annotations.Refreshable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@Refreshable
public class ImageRepositoryServiceImpl implements ImageRepositoryService {

    @Autowired
    private Collection<ImageRepository> imageRepositories;

    @Override
    public ImageRepository determineImageRepository(String code) {
        for (ImageRepository repository : imageRepositories) {
            if (repository.getCode().equals(code)) {
                return repository;
            }
        }
        return null;
    }
}
