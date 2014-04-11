package com.foreach.imageserver.core.business;

public class Context {
    private int id;

    /**
     * Careful: We use the code to generate an intelligible folder structure. Make sure that it can be used as a valid
     * folder name.
     */
    private String code;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
