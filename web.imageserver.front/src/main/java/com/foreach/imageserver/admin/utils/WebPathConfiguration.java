package com.foreach.imageserver.admin.utils;

import org.apache.commons.lang.StringUtils;

public class WebPathConfiguration {

    private String siteRoot;
    private String resources;

    public final String getResources()
    {
        return resources;
    }

    public final void setResources( String resources )
    {
        this.resources = removeTrailingSlash( resources );
    }

    public final String getSiteRoot()
    {
        return siteRoot;
    }

    public final void setSiteRoot( String siteRoot )
    {
        this.siteRoot = removeTrailingSlash( siteRoot );
    }

    private String removeTrailingSlash( String path )
    {
        return StringUtils.removeEnd( path, "/" );
    }
}
