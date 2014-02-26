package com.foreach.imageserver.connectors.dpp.ws;

import java.io.Serializable;

public interface WebServiceResult extends Serializable {
    //to be used as cache key, unique per WebService (this is just the request key)
    String getKey();

    void setKey( String key );

    void persist();
}
