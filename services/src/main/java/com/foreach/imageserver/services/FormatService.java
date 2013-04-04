package com.foreach.imageserver.services;

import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.image.Format;

import java.util.List;

public interface FormatService
{
    Format getFormatById( int id );

    List<Format> getFormatsByGroupId( int groupId );

	void saveFormat( Format format );

    void deleteFormat( int formatId );

    int getFormatIdForDimension(Size size, int groupId);
}
