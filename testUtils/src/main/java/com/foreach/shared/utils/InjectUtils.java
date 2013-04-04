package com.foreach.shared.utils;

import org.junit.Assert;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public final class InjectUtils {
    private static Map<Class, Map<String, Field>> fieldsByClass = new HashMap<Class, Map<String, Field>>();

    private InjectUtils()
    {
    }

    @SuppressWarnings("all")
    public static void inject( Object instance, String fieldName, Object fieldValue )
    {
        try {
            Class c = instance.getClass();
            Field field = getField( c, fieldName );
            field.setAccessible( true );
            field.set( instance, fieldValue );
        }
        catch ( IllegalAccessException t ) {
            Assert.fail( "Unable to inject value on field " + fieldName );
        }
        catch ( NoSuchFieldException t ) {
            Assert.fail("Unable to inject value on field " + fieldName);
        }
    }

    private static Field getField( Class c, String fieldName ) throws NoSuchFieldException
    {
        Map<String, Field> fields = fieldsByClass.get( c );

        if ( fields == null ) {
            fields = new HashMap<String, Field>();

            for ( Class sub = c; sub != null; sub = sub.getSuperclass() ) {
                for ( Field f : sub.getDeclaredFields() ) {
                    fields.put( f.getName(), f );
                }
            }
        }

        if ( !fields.containsKey( fieldName ) ) {
            throw new NoSuchFieldException( fieldName );
        }

        return fields.get( fieldName );
    }

}
