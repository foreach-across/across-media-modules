package com.foreach.imageserver.connectors.dpp.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class HttpWebServiceInvoker implements WebServiceInvoker {
    protected String username;
    protected String password;
    protected String baseurl;
    protected String contenttype;

    protected Logger log = LoggerFactory.getLogger( HttpWebServiceInvoker.class );

    //25 core threads, up to 25 on-demand threads (kept alive for 1 minute if no more demand), up to 100 tasks queued
    //not enough?  too much?  time will tell ...
    private ThreadPoolExecutor threadPool = new ThreadPoolExecutor( 25, 50, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>( 100 ) );
    private Set<String> runningRequests = Collections.synchronizedSet( new HashSet<String>() );

    public HttpWebServiceInvoker( String baseurl, String responseReaderEncoding ) {
        this.username = "";
        this.password = "";
        this.baseurl = baseurl;
    }

    public abstract WebServiceResult invoke( HttpWebServiceRequest request );

    @Override
    public WebServiceResult invoke( WebServiceRequest request ) {
        return invoke( ( HttpWebServiceRequest ) request );
    }

    public abstract WebServiceResult getResultWithStream( HttpWebServiceRequest request, InputStream is, int responseCode );

    public void modifyThreadPoolSize( int core, int max ) {
        threadPool.setCorePoolSize( core );
        threadPool.setMaximumPoolSize( max );
    }
}
