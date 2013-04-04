package com.foreach.imageserver.dao;

import com.foreach.imageserver.business.image.Format;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormatDao
{
    Format getFormatById( int id );

    void insertFormat( Format format );

    void updateFormat( Format format );

    void deleteFormat( int id );

    List<Format> getFormatsByGroupId( int groupId );
}
