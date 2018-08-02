package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.business.Image;

public interface ImageRepositoryCustom
{
	void update( Image object );

	void create( Image object );
}
