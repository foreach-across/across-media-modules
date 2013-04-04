package com.foreach.imageserver.business.taxonomy;

public class Application
{
    private int id;
    private String name;
    private String callbackUrl;

    public final int getId()
    {
        return id;
    }

    public final void setId(int id)
    {
        this.id = id;
    }

    public final String getName()
    {
        return name;
    }

    public final void setName(String name)
    {
        this.name = name;
    }

    public final String getCallbackUrl()
    {
        return callbackUrl;
    }

    public final void setCallbackUrl(String callbackUrl)
    {
        this.callbackUrl = callbackUrl;
    }
}
