package com.foreach.imageserver.utils.enums;

public enum Status {

    SUCCESS("success"),
    FEEDBACK("feedback"),
    ERROR("error");

    private String description;

    Status(String description) {
        this.description = description;
    }

    public final String getDescription() {
        return description;
    }
}
