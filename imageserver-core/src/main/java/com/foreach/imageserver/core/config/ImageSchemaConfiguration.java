package com.foreach.imageserver.core.config;

import com.foreach.across.core.database.SchemaConfiguration;
import com.foreach.across.core.database.SchemaObject;

import java.util.Arrays;

public class ImageSchemaConfiguration extends SchemaConfiguration {
    public static final String TABLE_CONTEXT = "img_context";
    public static final String TABLE_IMAGE = "img_image";
    public static final String TABLE_IMAGE_RESOLUTION = "img_image_resolution";
    public static final String TABLE_IMAGE_MODIFICATION = "img_image_modification";
    public static final String TABLE_IMAGE_PROFILE = "img_image_profile";
    public static final String TABLE_IMAGE_PROFILE_MODIFICATION = "img_image_profile_modification";

    public ImageSchemaConfiguration() {
        super( Arrays.asList(new SchemaObject("table.context", TABLE_CONTEXT),
                new SchemaObject("table.image", TABLE_IMAGE),
                new SchemaObject("table.image_resolution", TABLE_IMAGE_RESOLUTION),
                new SchemaObject("table.image_modification", TABLE_IMAGE_MODIFICATION),
                new SchemaObject("table.image_profile", TABLE_IMAGE_PROFILE),
                new SchemaObject("table.image_profile_modification", TABLE_IMAGE_PROFILE_MODIFICATION)
                )
        );
    }
}
