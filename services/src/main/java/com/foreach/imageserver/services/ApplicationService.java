package com.foreach.imageserver.services;

import com.foreach.imageserver.business.Application;

import java.util.Collection;

public interface ApplicationService
{
	Application getApplicationById( int id );

	Collection<Application> getAllApplications();
}
