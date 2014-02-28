package com.foreach.imageserver.connectors.dpp.ws;

public interface WebServiceRequest {
    /**Before the webservice can be invoked, some parameters will have to be set, but this happens in an implementation-specific way
     * HTTP services will need just an URL and a HashMap for params, but SOAP services can use complexer XML
     */

    /**
     * @return Key (to be used for cache etc, uniquely identifies this request)
     */
    String getKey();

    WebServiceResult invoke();

    WebServiceInvoker getInvoker();

    /**
     * Returns the name of the webservice
     * Can be used as a prefix for the cache-keys etc
     *
     * @return Name
     */
    String getServiceName();
}
