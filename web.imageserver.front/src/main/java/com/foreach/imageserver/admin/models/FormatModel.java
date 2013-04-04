package com.foreach.imageserver.admin.models;

import com.foreach.imageserver.business.image.Format;

public class FormatModel {

    private int id;
    private String name;
    private int width;
    private boolean available;
    private boolean different;

    public FormatModel( Format format )
    {
        this.id = format.getId();
        this.name = format.getName();
        this.width = format.getDimensions().getWidth();
    }

    public final int getId()
    {
        return id;
    }

    public final void setId( int id )
    {
        this.id = id;
    }

    public final String getName()
    {
        return name;
    }

    public final void setName( String name )
    {
        this.name = name;
    }

    public final int getWidth()
    {
        return width;
    }

    public final void setWidth(int width)
    {
        this.width = width;
    }

    public final boolean isAvailable()
    {
        return available;
    }

    public final void setAvailable( boolean available )
    {
        this.available = available;
    }

    public final boolean isDifferent()
    {
        return different;
    }

    public final void setDifferent( boolean different )
    {
        this.different = different;
    }

    @Override
    public final String toString()
    {
        return getName();
    }
}
