package com.foreach.imageserver.admin.viewHelpers;

import com.foreach.imageserver.business.taxonomy.Application;

public class ApplicationViewHelper {

    private final Application application;
    private int numberOfGroups;
    private int numberOfImages;

    public ApplicationViewHelper(Application application) {
        this.application = application;
    }

    public final int getId(){
        return application.getId();
    }

    public final String getName(){
        return application.getName();
    }

    public final int getNumberOfGroups() {
        return numberOfGroups;
    }

    public final void setNumberOfGroups(int numberOfGroups) {
        this.numberOfGroups = numberOfGroups;
    }

    public final int getNumberOfImages() {
        return numberOfImages;
    }

    public final void setNumberOfImages(int numberOfImages) {
        this.numberOfImages = numberOfImages;
    }
}
