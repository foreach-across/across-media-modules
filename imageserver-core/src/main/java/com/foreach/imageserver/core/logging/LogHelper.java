package com.foreach.imageserver.core.logging;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class LogHelper {

    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private LogHelper() {
    }

    public static String[] flatten(Object... objects) {
        String[] result = new String[objects.length];
        for (int i = 0; i < objects.length; i++) {
            result[i] = flatten(objects[i]);
        }
        return result;
    }

    public static String flatten(Object object) {
        if (object instanceof Number || object instanceof String || object == null) {
            return String.valueOf(object);
        }
        if (object instanceof Date) {
            return DATE_FORMAT.format(object);
        }
        return ToStringBuilder.reflectionToString(object, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
