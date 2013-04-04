package com.foreach.imageserver.admin.editors;

import com.foreach.imageserver.business.math.Fraction;

import java.beans.PropertyEditorSupport;

public class FractionEditor extends PropertyEditorSupport
{
    private Fraction fraction = null;

    @Override
    public final Object getValue()
    {
        return fraction;
    }

    @Override
    public final void setValue( Object object )
    {
        fraction = (Fraction) object;
    }

    public final String getAsText()
    {
        return ( fraction != null ) ? fraction.getStringForUrl() : "";
    }

    public final void setAsText( String text) {

        Fraction parsedFraction = null;

        try {
            parsedFraction = Fraction.parseString( text );
        } catch ( NumberFormatException nf ) {

        }

        fraction = parsedFraction;
    }
}
