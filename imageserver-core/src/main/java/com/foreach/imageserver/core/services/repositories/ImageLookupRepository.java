package com.foreach.imageserver.core.services.repositories;

public interface ImageLookupRepository {
    /**
     * Verifies that a URI specified identifies an image in the repository.
     * This only checks the format of the URI, does not check the repository if the image exists.
     *
     * @param uri URI of the image.
     * @return True if URI is recognized by the repository.
     */
    boolean isValidURI(String uri);

    /**
     * Looks up the image matching the given URI in the repository.
     *
     * @param uri URI of the image.
     * @return RepositoryLookupResult contaning status and image data (if found).
     */
    RepositoryLookupResult fetchImage(String uri);
}
