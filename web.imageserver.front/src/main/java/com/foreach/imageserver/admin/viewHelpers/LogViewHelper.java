package com.foreach.imageserver.admin.viewHelpers;

import com.foreach.imageserver.business.image.VariantImage;
import org.apache.commons.lang.time.FastDateFormat;

import java.util.Locale;
import java.util.TimeZone;

public class LogViewHelper {

    private VariantImage variantImage;

    public LogViewHelper(VariantImage variantImage) {
        this.variantImage = variantImage;
    }

    public final String getDescription(){
        StringBuilder path = new StringBuilder( )
                .append( "image id: " )
                .append( variantImage.getImageId() )
                .append("  variant: ")
                .append( variantImage.getWidth() )
                .append("x")
                .append( variantImage.getHeight() )
                .append( "  version: " )
                .append( variantImage.getVersion() )
                .append( "  last called: " )
                .append(getDate());

		return path.toString();
    }

    public final String getDate(){
        FastDateFormat dateFormat = FastDateFormat.getInstance( "HH:mm dd/MM/yyyy", TimeZone.getDefault(), Locale.getDefault() );
        return dateFormat.format(variantImage.getLastCalled() != null ? variantImage.getLastCalled() : "00:00 00/00/0000");
    }
}
