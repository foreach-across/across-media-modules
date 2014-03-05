package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.business.ImageVariant;
import com.foreach.imageserver.core.data.ImageVariantDao;
import com.foreach.imageserver.core.web.dto.ImageModificationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImageVariantServiceImpl implements ImageVariantService {

    @Autowired
    private ImageVariantDao imageVariantDao;

    @Override
    public List<ImageVariant> getRegisteredVariantsForApplication(Application application) {
        return imageVariantDao.getVariantsForApplication(application.getId());
    }

    @Override
    public ImageVariant getBestVariantForModification(Application application, ImageModificationDto modificationDto) {
        List<ImageVariant> allVariants = imageVariantDao.getVariantsForApplication(application.getId());
        return findBestVariant(allVariants, modificationDto);
    }

    @Override
    public ImageVariant getExactVariantForModification(Application application, ImageModificationDto modificationDto) {
        List<ImageVariant> allVariants = imageVariantDao.getVariantsForApplication(application.getId());
        for (ImageVariant variant : allVariants) {
            if (matches(modificationDto, variant)) {
                return variant;
            }
        }
        return null;
    }

    /**
     * Check whether the variant matches with the given parameters, we take into account width, height, density,
     * keepAspect and stretch. We don't take into account output type
     */

    private boolean matches(ImageModificationDto modificationDto, ImageVariant variant) {
        return modificationDto.getHeight() == variant.getHeight() &&
                modificationDto.getWidth() == variant.getWidth() &&
                modificationDto.getDensity().equals(variant.getDensity()) &&
                modificationDto.isKeepAspect() == variant.isKeepAspect() &&
                modificationDto.isStretch() == variant.isStretch()
                ;

    }

    private ImageVariant findBestVariant(List<ImageVariant> allVariants, ImageModificationDto modificationDto) {
        //TODO: make sure this does somewhat the same as the site

        ImageVariant best = null;

        if (modificationDto.getWidth() > 0) {
            for (ImageVariant variant : allVariants) {
                if (variant.getWidth() <= modificationDto.getWidth() && (variant.getHeight() * modificationDto.getHeight() == 0 || variant.getHeight() <= modificationDto.getHeight())) {
                    if (best == null || best.getWidth() < variant.getWidth()) {
                        best = variant;
                    }
                }
            }
        } else {
            //No width given, use height
            for (ImageVariant variant : allVariants) {
                if (variant.getHeight() <= modificationDto.getHeight()) {
                    if (best == null || best.getHeight() < variant.getHeight()) {
                        best = variant;
                    }
                }
            }
        }

        return best;
    }
}
