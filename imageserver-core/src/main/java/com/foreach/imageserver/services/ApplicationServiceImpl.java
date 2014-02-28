package com.foreach.imageserver.services;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.data.ApplicationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ApplicationServiceImpl implements ApplicationService
{
	@Autowired
	private ApplicationDao applicationDao;

	public final Application getApplicationById( int id ) {
		return applicationDao.getApplicationById( id );
	}

	public final Collection<Application> getAllApplications() {
		return applicationDao.getAllApplications();
	}
}
