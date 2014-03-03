package com.foreach.imageserver.core.web.context;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WebPathConfiguration {
    @Value("${path.siteRoot}")
    private String siteRoot;

    @Value("${path.resources}")
    private String resources;

    public final String getResources() {
        return resources;
    }

    public final void setResources(String resources) {
        this.resources = removeTrailingSlash(resources);
    }

    public final String getSiteRoot() {
        return siteRoot;
    }

    public final void setSiteRoot(String siteRoot) {
        this.siteRoot = removeTrailingSlash(siteRoot);
    }

    private String removeTrailingSlash(String path) {
        return StringUtils.removeEnd(path, "/");
    }
}
