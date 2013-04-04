package com.foreach.imageserver.services;

import com.foreach.imageserver.business.taxonomy.Application;
import com.foreach.imageserver.dao.ApplicationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationServiceImpl implements ApplicationService
{
    @Autowired
    private ApplicationDao applicationDao;

    public final Application getApplicationById(int id)
    {
        return applicationDao.getApplicationById( id );
    }
    
    public final List<Application> getAllApplications()
    {
        return applicationDao.getAllApplications();
    }
}
