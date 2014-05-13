package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.data.ImageResolutionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class ImageResolutionManagerImpl implements ImageResolutionManager {
    private static final String CACHE_NAME = "imageResolutions";

    private final ImageResolutionDao imageResolutionDao;

    @Autowired
    public ImageResolutionManagerImpl(ImageResolutionDao imageResolutionDao, CacheManager cacheManager) {
        this.imageResolutionDao = imageResolutionDao;

        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            throw new RuntimeException(String.format("Required cache %s is unavailable.", CACHE_NAME));
        }
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'byId-'+#resolutionId")
    public ImageResolution getById(int resolutionId) {
        return imageResolutionDao.getById(resolutionId);
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'forContext-'+#contextId")
    public List<ImageResolution> getForContext(int contextId) {
        return Collections.unmodifiableList(imageResolutionDao.getForContext(contextId));
    }
}
