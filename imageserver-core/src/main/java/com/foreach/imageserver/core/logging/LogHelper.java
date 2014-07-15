package com.foreach.imageserver.core.logging;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class LogHelper {

    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private LogHelper() {
    }

    public static String[] asStringArray(Object... objects) {
        String[] result = new String[objects.length];
        for (int i = 0; i < objects.length; i++) {
            Object object = objects[i];
            if (object instanceof Number || object instanceof String || object == null) {
                result[i] = String.valueOf(object);
            } else if (object instanceof Date) {
                result[i] = DATE_FORMAT.format(object);
            } else {
                result[i] = ToStringBuilder.reflectionToString(objects[i], ToStringStyle.SHORT_PREFIX_STYLE);
            }
        }
        return result;
    }
}
