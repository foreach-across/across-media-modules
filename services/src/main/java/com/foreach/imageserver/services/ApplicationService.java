package com.foreach.imageserver.services;

import com.foreach.imageserver.business.Application;

import java.util.Collection;
import java.util.UUID;

public interface ApplicationService
{
	Application getApplicationById( int id );

	Collection<Application> getAllApplications();
}
