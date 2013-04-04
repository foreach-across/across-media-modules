package com.foreach.imageserver.business.math;

import java.util.*;

public final class Category {

    private Category()
    {

    }

    public static <V> void filter( Collection<V> src, Collection<V> dst, Predicate<V> pred )
    {
        for( V v : src ) {
            if( pred.appliesTo( v ) ) {
                dst.add( v );
            }
        }
    }

    public static <V,W> void project( Collection<V> src, Set<W> dst, Function<V,W> f )
    {
        for( V v : src ) {
            dst.add( f.valueFor( v ) );
        }
    }

    public static <V,W extends Comparable<? super W>> void project( Collection<V> src, List<W> dst, Function<V,W> f )
    {
        Set<W> intermediate = new HashSet<W>();
        project( src, intermediate, f );
        dst.addAll( intermediate );
        Collections.sort( dst );
    }


    public static <V,W> void preImage( Collection<V> src, Collection<V> dst, final Function<V,W> f, final W img )
    {
        filter( src, dst, new Predicate<V>() {
            public boolean appliesTo( V v ){
                return  f.valueFor( v ).equals( img );
            }
        });
    }


    /*
        Given a Collection of V and a function V -> W,
        create the inverse function of W -> List<V> as a Map<W,List<V>>.


        E.g. given a List of Strings and a requirement to group them by size,
         you would typically code something like


        Map<Integer,List<String>> mapBySize = new HashMap<Integer,List<String>>();

        for( String s : stringList) {

            int len = s.length();

            if( ! mapBySize.containsKey( length ) ) {
                mapBySize.put( length, new ArrayList<String>() );
            }

            mapBySize.get( length ).add( s );
        }


        All the boilerplate code, except the function, is abstracted into Category, so you can now write


        Map<Integer,Collection<String>> mapBySize = new HashMap<Integer,Collection<String>>();

        Category.invert( stringList , mapBySize, new Function<String,Integer>(){
            public Integer valueFor( String s ) {
                return s.length();
            }
        });


        to the same effect.
    */

    public static <V,W> void invert( Collection<V> src, Map<W,Collection<V>> dst, Function<V,W> f )
    {
        for( V v : src) {

            W w = f.valueFor( v );

            if( ! dst.containsKey( w ) ) {
                dst.put( w, new ArrayList<V>() );
            }

            dst.get( w ).add( v );
        }
    }
}
