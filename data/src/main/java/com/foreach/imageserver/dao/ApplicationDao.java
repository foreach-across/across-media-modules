package com.foreach.imageserver.dao;

import com.foreach.imageserver.business.taxonomy.Application;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationDao
{
    Application getApplicationById(int id);

    List<Application> getAllApplications();

    void insertApplication( Application application );

    void updateApplication( Application application );

    void deleteApplication( int applicationId );
}
