package com.foreach.imageserver.connectors.dpp.ws;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public abstract class HttpWebServiceRequest implements WebServiceRequest {

    private Map<String, String> headers = new HashMap<String, String>();
    private String requestMethod;
    private InputStream inputStream;
    private long contentlength = -1;

    /*
    * If false the result will be handled by default as text content, this content will be passed as a String to a result class
    * If true the result will be handled as an input stream which will be passed to a result class
    * */
    private boolean interceptResultStream = false;

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setContent(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void setContentLength(long contentlength) {
        this.contentlength = contentlength;
    }

    public InputStream getContent() {
        return inputStream;
    }

    public long getContentLength() {
        return contentlength;
    }

    public boolean isInterceptResultStream() {
        return interceptResultStream;
    }

    public void setInterceptResultStream(boolean interceptResultStream) {
        this.interceptResultStream = interceptResultStream;
    }

    /**
     * @return All extra params needed, in addition to the baseurl
     */
    public abstract String getExtraParams();

    @Override
    public WebServiceResult invoke() {
        return getInvoker().invoke(this);
    }

    @Override
    public String getKey() {
        return getServiceName() + "-" + getExtraParams();
    }
}
