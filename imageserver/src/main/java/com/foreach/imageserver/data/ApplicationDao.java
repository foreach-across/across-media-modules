package com.foreach.imageserver.data;

import com.foreach.imageserver.business.Application;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationDao
{
	Application getApplicationById( int id );

	List<Application> getAllApplications();

	void insertApplication( Application application );

	void updateApplication( Application application );

	void deleteApplication( int applicationId );
}
