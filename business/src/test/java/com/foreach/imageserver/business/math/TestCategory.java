package com.foreach.imageserver.business.math;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class TestCategory {

    @Test
    public void strLen()
    {
        List<String> src = Arrays.asList(
                "foo",
                "bar",
                "foobar"
        );

        Map<Integer,Collection<String>> dst = new HashMap<Integer,Collection<String>>();

        Category.invert( src, dst, new Function<String,Integer>(){
            public Integer valueFor( String s ) {
                return s.length();
            }
        });

        Assert.assertEquals( 2, dst.keySet().size() );

        Assert.assertEquals( 2, dst.get( 3 ).size() );
        Assert.assertEquals( true, dst.get( 3 ).contains( "foo" ) );
        Assert.assertEquals( true, dst.get( 3 ).contains( "bar" ) );
        Assert.assertEquals( 1, dst.get( 6 ).size() );
        Assert.assertEquals( true, dst.get( 6 ).contains( "foobar" ) );
    }
}
