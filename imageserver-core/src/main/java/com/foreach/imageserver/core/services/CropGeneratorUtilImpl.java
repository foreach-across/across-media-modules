package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.dto.CropDto;
import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.math.AspectRatio;
import com.foreach.imageserver.math.ImageServerConversionUtils;
import org.springframework.stereotype.Component;

@Component
public class CropGeneratorUtilImpl implements CropGeneratorUtil
{
	/**
	 * Normalizes a modification to be possible with the defined original image.
	 */
	@Override
	public void normalizeModificationDto( Image image, ImageModificationDto imageModificationDto ) {
		// Determine the actual resolution requested based on the original
		Dimensions originalDimensions = image.getDimensions();
		Dimensions targetDimensions
				= new Dimensions( imageModificationDto.getResolution().getWidth(),
				                  imageModificationDto.getResolution().getHeight() )
				.normalize( image.getDimensions() );

		// If boundaries are specified, scale down the output to fit in the boundaries
		if ( imageModificationDto.hasBoundaries() ) {
			targetDimensions =
					targetDimensions.scaleToFitIn( DtoUtil.toBusiness( imageModificationDto.getBoundaries() ) );
		}

		if ( !imageModificationDto.hasCrop() ) {
			imageModificationDto.setCrop( determineDefaultCrop( originalDimensions, targetDimensions ) );
		}
		else {
			// Translate the crop according to the actual original
			normalizeCrop( originalDimensions, imageModificationDto.getCrop() );
		}

		imageModificationDto.setResolution(
				new ImageResolutionDto( targetDimensions.getWidth(), targetDimensions.getHeight() ) );
		imageModificationDto.setBoundaries( new DimensionsDto() );

		//calculateDensity( imageModificationDto, originalDimensions );
	}

	private CropDto determineDefaultCrop( Dimensions originalDimensions, Dimensions targetDimensions ) {
		AspectRatio targetRatio = targetDimensions.fetchAspectRatio();
		if ( !originalDimensions.fetchAspectRatio().equals( targetRatio ) ) {
			int width = originalDimensions.getWidth();
			int height = originalDimensions.getHeight();

			if ( targetRatio.isLargerOnWidth() ) {
				height = targetRatio.calculateHeightForWidth( width );
			}
			else {
				width = targetRatio.calculateWidthForHeight( height );
			}

			DimensionsDto cropDimensions = ImageServerConversionUtils.scaleToFitIn(
					new DimensionsDto( width, height ),
					new DimensionsDto( originalDimensions.getWidth(), originalDimensions.getHeight() )
			);
			int x = ( originalDimensions.getWidth() - cropDimensions.getWidth() ) / 2;
			int y = ( originalDimensions.getHeight() - cropDimensions.getHeight() ) / 2;
			return new CropDto( x, y, cropDimensions.getWidth(), cropDimensions.getHeight() );
		}

		// simply scale the image
		return new CropDto( 0, 0, originalDimensions.getWidth(), originalDimensions.getHeight() );
	}

	private void normalizeCrop( Dimensions originalDimensions, CropDto crop ) {
		// Adjust source dimensions based on original aspect ratio if necessary
		Dimensions source = DtoUtil.toBusiness( crop.getSource() ).normalize( originalDimensions );

		if ( crop.hasBox() ) {
			// Adjust box dimensions based on original aspect ratio if necessary
			Dimensions box = DtoUtil.toBusiness( crop.getBox() ).normalize( originalDimensions );

			// Ensure the source dimensions fit in the box
			source = source.scaleToFitIn( box );
		}

		// Box is no longer relevant as source is set
		crop.setBox( new DimensionsDto() );
		crop.setSource( DtoUtil.toDto( source ) );

		// Translate the crop coordinates to fit with the actual original and make sure the crop is valid
		translateSource( crop, originalDimensions );

		if ( crop.getWidth() == 0 || crop.getHeight() == 0 ) {
			throw new RuntimeException( "Could not process crop: resulted in an illegal width or height of 0" );
		}
	}

	private void translateSource( CropDto crop, Dimensions originalDimensions ) {
		int leftX = crop.getX();
		int leftY = crop.getY();
		int rightX = leftX + crop.getWidth();
		int rightY = leftY + crop.getHeight();

		DimensionsDto source = crop.getSource();

		leftX = snap( leftX, source.getWidth() );
		leftY = snap( leftY, source.getHeight() );
		rightX = snap( rightX, source.getWidth() );
		rightY = snap( rightY, source.getHeight() );

		if ( !originalDimensions.equals( DtoUtil.toBusiness( source ) ) ) {
			double modX = (double) originalDimensions.getWidth() / source.getWidth();
			double modY = (double) originalDimensions.getHeight() / source.getHeight();

			crop.setX( Double.valueOf( leftX * modX ).intValue() );
			crop.setY( Double.valueOf( leftY * modY ).intValue() );
			crop.setWidth( Double.valueOf( ( rightX - leftX ) * modX ).intValue() );
			crop.setHeight( Double.valueOf( ( rightY - leftY ) * modY ).intValue() );
		}
		else {
			crop.setX( leftX );
			crop.setY( leftY );
			crop.setWidth( rightX - leftX );
			crop.setHeight( rightY - leftY );
		}

		crop.setSource( new DimensionsDto() );
	}

	private int snap( int pos, int max ) {
		if ( pos < 0 ) {
			return 0;
		}
		else if ( pos > max ) {
			return max;
		}
		return pos;
	}

	// todo: remove once ImageTransformDto features have been thoroughly tested
	@SuppressWarnings( "unused" )
	private void calculateDensity( ImageModificationDto normalized, Dimensions original ) {
		if ( new DimensionsDto().equals( normalized.getDensity() ) ) {
			DimensionsDto calculated = new DimensionsDto();

			int requestedWidth = normalized.getResolution().getWidth();
			int requestedHeight = normalized.getResolution().getHeight();

			int originalWidth = normalized.hasCrop() ? normalized.getCrop().getWidth() : original.getWidth();
			int originalHeight = normalized.hasCrop() ? normalized.getCrop().getHeight() : original.getHeight();

			if ( originalWidth >= requestedWidth ) {
				calculated.setWidth( 1 );
			}
			else {
				calculated.setWidth(
						Double.valueOf( Math.ceil( requestedWidth / (double) originalWidth ) ).intValue() );
			}
			if ( originalHeight >= requestedHeight ) {
				calculated.setHeight( 1 );
			}
			else {
				calculated.setHeight(
						Double.valueOf( Math.ceil( requestedHeight / (double) originalHeight ) ).intValue() );
			}

			normalized.setDensity( calculated );
		}
	}

	@Override
	public Dimensions applyResolution( Image image, ImageResolution resolution ) {
		return resolution.getDimensions().normalize( image.getDimensions() );
		/*
		Integer resolutionWidth = resolution.getWidth();
        Integer resolutionHeight = resolution.getHeight();

        if (resolutionWidth != null && resolutionHeight != null) {
            return new Dimensions(resolutionWidth, resolutionHeight);
        } else {
            double originalWidth = image.getDimensions().getWidth();
            double originalHeight = image.getDimensions().getHeight();

            if (resolutionWidth != null) {
                return new Dimensions(resolutionWidth, (int) Math.round(resolutionWidth * (originalHeight / originalWidth)));
            } else {
                return new Dimensions((int) Math.round(resolutionHeight * (originalWidth / originalHeight)), resolutionHeight);
            }
        }*/
	}

	@Override
	public int area( Crop crop ) {
		return crop.getWidth() * crop.getHeight();
	}

	@Override
	public Crop intersect( Crop crop1, Crop crop2 ) {
		int l1 = crop1.getX();
		int r1 = crop1.getX() + crop1.getWidth();
		int t1 = crop1.getY();
		int b1 = crop1.getY() + crop1.getHeight();

		int l2 = crop2.getX();
		int r2 = crop2.getX() + crop2.getWidth();
		int t2 = crop2.getY();
		int b2 = crop2.getY() + crop2.getHeight();

		if ( l2 > r1 || r2 < l1 || t2 > b1 || b2 < t1 ) {
			// No intersection.
			return null;
		}

		int li = Math.max( l1, l2 );
		int ri = Math.min( r1, r2 );
		int ti = Math.max( t1, t2 );
		int bi = Math.min( b1, b2 );

		return new Crop( li, ti, ri - li, bi - ti );
	}

}
