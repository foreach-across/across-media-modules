package com.foreach.imageserver.core.repositories;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;

public class BasicRepositoryImpl<T> implements BasicRepository<T>
{
	final Class<T> clazz;

	@Autowired
	private SessionFactory sessionFactory;

	@SuppressWarnings("unchecked")
	public BasicRepositoryImpl() {
		ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
		this.clazz = (Class<T>) genericSuperclass.getActualTypeArguments()[0];
	}

	protected Session session() {
		return sessionFactory.getCurrentSession();
	}

	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	@Override
	public T getById( long id ) {
		return (T) session().get( clazz, id );
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	@Override
	public Collection<T> getAll() {
		return (Collection<T>) session().createCriteria( clazz ).setResultTransformer(
				Criteria.DISTINCT_ROOT_ENTITY ).list();
	}

	@Transactional
	@Override
	public void create( T object ) {
		session().save( object );
	}

	@Transactional
	@Override
	public void update( T object ) {
		session().update( object );
	}

	@Transactional
	@Override
	public void delete( T object ) {
		session().delete( object );
	}
}
