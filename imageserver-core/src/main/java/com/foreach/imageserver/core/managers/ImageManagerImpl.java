package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.data.ImageDao;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Repository
public class ImageManagerImpl implements ImageManager {
    private static final String CACHE_NAME = "images";

    private final ImageDao imageDao;

    @Autowired
    public ImageManagerImpl(ImageDao imageDao, CacheManager cacheManager) {
        this.imageDao = imageDao;

        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            throw new RuntimeException(String.format("Required cache %s is unavailable.", CACHE_NAME));
        }
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "T(com.foreach.imageserver.core.managers.ImageManagerImpl).byIdKey(#imageId)")
    public Image getById(int imageId) {
        return imageDao.getById(imageId);
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "T(com.foreach.imageserver.core.managers.ImageManagerImpl).byExternalIdKey(#externalId)")
    public Image getByExternalId(String externalId) {
        return imageDao.getByExternalId(externalId);
    }

    @Override
    public void insert(Image image) {
        imageDao.insert(image);
    }

    public static String byIdKey(int imageId) {
        return "byId-" + imageId;
    }

    public static String byExternalIdKey(String externalId) {
        return "byExternalId-" + externalId;
    }
}
