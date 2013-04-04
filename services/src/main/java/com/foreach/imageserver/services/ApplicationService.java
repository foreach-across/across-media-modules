package com.foreach.imageserver.services;

import com.foreach.imageserver.business.taxonomy.Application;

import java.util.List;

public interface ApplicationService
{
    Application getApplicationById(int id);
    List<Application> getAllApplications();
}
