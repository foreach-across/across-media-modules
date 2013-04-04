package com.foreach.imageserver.admin.models;

import com.foreach.imageserver.business.image.Format;

import java.util.ArrayList;
import java.util.List;

public class FormatModels
{
	private FormatModel generic;

	private List<FormatModel> specificFormats;

	private FormatModel selected;

	public FormatModels( List<Format> formats, int targetWidth )
	{
		specificFormats = new ArrayList<FormatModel>();

		for( Format format : formats ) {

			FormatModel model = new FormatModel( format );
			int formatWidth = format.getDimensions().getWidth();
			if( formatWidth == 0 ) {
				generic = model;
			} else {
				specificFormats.add( model );
			}

			if( format.getDimensions().getWidth() == targetWidth ) {
				selected = model;
			}
		}

		if( selected == null ) {
			selected = generic;
		}
	}

	public final FormatModel getGeneric()
	{
		return generic;
	}

	public final FormatModel getSelected()
	{
		return selected;
	}

	public final List<FormatModel> getSpecificFormats()
	{
		return specificFormats;
	}
}
