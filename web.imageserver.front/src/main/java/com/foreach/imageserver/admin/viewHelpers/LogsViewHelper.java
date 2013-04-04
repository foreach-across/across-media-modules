package com.foreach.imageserver.admin.viewHelpers;

import com.foreach.imageserver.business.image.VariantImage;

import java.util.ArrayList;
import java.util.List;

public class LogsViewHelper {

    private List<LogViewHelper> list;

    public LogsViewHelper(List<VariantImage> variantImages) {

        this.list = new ArrayList<LogViewHelper>();

        for (VariantImage variantImage : variantImages) {
            LogViewHelper logViewHelper = new LogViewHelper(variantImage);
            list.add(logViewHelper);
        }
    }

    public final List<LogViewHelper> getList() {
        return list;
    }

    public final void setList(List<LogViewHelper> list) {
        this.list = list;
    }
}
