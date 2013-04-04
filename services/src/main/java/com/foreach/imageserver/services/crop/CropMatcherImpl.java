package com.foreach.imageserver.services.crop;

import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.image.Crop;

import com.foreach.imageserver.business.math.Fraction;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class CropMatcherImpl implements CropMatcher
{

    private int matchingFunction( Crop crop, Fraction aspectRatio, int width )
    {
        if( crop.hasAspectRatio() && crop.getAspectRatio().equals( aspectRatio ) ) {

            if( crop.getTargetWidth() == width ) {
                return 3;
            }

            if( crop.getTargetWidth() == 0 ) {
                return 2;
            }
        }

        return 0;
    }

    private Crop bestCropWithMatchingVersionFrom( Set<Crop> crops, int version, Fraction aspectRatio, int width )
    {
        Crop currentMatch = null;
        int currentGrade = 0;

        for( Crop crop : crops ){
            if( crop.getVersion() == version ) {

                int grade = matchingFunction( crop, aspectRatio, width );
                if ( grade > currentGrade ) {
                    currentGrade = grade;
                    currentMatch = crop;
                }
            }
        }

        return currentMatch;
    }

    public final Crop bestCropFrom( Set<Crop> crops, int version, Size size )
    {
        return bestCropFrom( crops, version, size.aspectRatio(), size.getWidth() );
    }

    public final Crop bestCropFrom( Set<Crop> crops, int version, Fraction aspectRatio, int width  )
    {
        Crop crop = bestCropWithMatchingVersionFrom( crops, version, aspectRatio, width );

        // If we didn't find a crop, try version 0, unless that was specified
        if ( ( crop == null ) && ( version != 0 ) ) {

            crop = bestCropWithMatchingVersionFrom( crops, 0, aspectRatio, width );
        }
        return crop;
    }

}
