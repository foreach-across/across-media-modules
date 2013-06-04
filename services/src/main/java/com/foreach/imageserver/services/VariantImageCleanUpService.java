package com.foreach.imageserver.services;

import com.foreach.imageserver.business.image.Format;
import com.foreach.imageserver.business.Application;
import com.foreach.imageserver.business.taxonomy.Group;

public interface VariantImageCleanUpService {

    void cleanUpVariantsForApplication( Application application );

    void cleanUpVariantsForGroup( Group group );

    void cleanUpVariantsForFormat( Format format );
}
