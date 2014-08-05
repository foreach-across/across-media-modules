package com.foreach.imageserver.core.repositories;

import java.util.Collection;

public interface BasicRepository<T>
{
	T getById( long id );

	Collection<T> getAll();

	void create( T object );

	void update( T object );

	void delete( T object );

}
