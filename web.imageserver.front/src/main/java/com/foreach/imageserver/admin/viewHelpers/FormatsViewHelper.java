package com.foreach.imageserver.admin.viewHelpers;

import com.foreach.imageserver.business.image.Format;

import java.util.List;

public class FormatsViewHelper {
    private List<Format> list;

    public FormatsViewHelper(List<Format> formats) {
        this.list = formats;
    }

    public final List<Format> getList() {
        return list;
    }

    public final void setList(List<Format> list) {
        this.list = list;
    }

    public final int getNumberOfFormats() {
        return list.size();
    }
}
