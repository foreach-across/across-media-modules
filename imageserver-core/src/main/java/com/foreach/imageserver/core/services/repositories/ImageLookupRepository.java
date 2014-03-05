package com.foreach.imageserver.core.services.repositories;

import java.util.Map;

public interface ImageLookupRepository {
    /**
     * Get the code for this repository (e.g. url, dio, ...)
     */
    String getCode();

    /**
     * Looks up the image matching the given URI in the repository.
     *
     * @param parameters to fetch the image
     * @return RepositoryLookupResult contaning status and image data (if found).
     */
    RepositoryLookupResult fetchImage(Map<String,String> parameters);
}
