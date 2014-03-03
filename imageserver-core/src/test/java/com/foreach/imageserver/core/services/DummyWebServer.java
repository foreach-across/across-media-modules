package com.foreach.imageserver.core.services;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DummyWebServer {
    private static final int SERVER_PORT = 13333;

    private static final String PATH_NOT_FOUND = "/404";
    private static final String PATH_DENIED = "/denied";
    private static final String PATH_ERROR = "/500";

    private Server server = new Server(SERVER_PORT);

    public void start() throws Exception {
        server.setHandler(new TestImageResourceHandler());
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public String notFoundUrl() {
        return createUrl(PATH_NOT_FOUND);
    }

    public String permissionDeniedUrl() {
        return createUrl(PATH_DENIED);
    }

    public String errorUrl() {
        return createUrl(PATH_ERROR);
    }

    public String imageUrl(ImageTestData imageTestData) {
        return createUrl("/image/" + imageTestData.toString());
    }

    private String createUrl(String path) {
        return "http://localhost:" + SERVER_PORT + path;
    }

    public static class TestImageResourceHandler extends AbstractHandler {
        public void handle(String target,
                           Request baseRequest,
                           HttpServletRequest request,
                           HttpServletResponse response) throws IOException, ServletException {

            if (StringUtils.equals(PATH_NOT_FOUND, target)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } else if (StringUtils.equals(PATH_DENIED, target)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            } else if (StringUtils.equals(PATH_ERROR, target)) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } else if (StringUtils.startsWith(target, "/image/")) {
                ImageTestData imageTestData = ImageTestData.valueOf(StringUtils.replaceOnce(target, "/image/", ""));

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType(imageTestData.getContentType());

                IOUtils.copy(getClass().getResourceAsStream(imageTestData.getResourcePath()), response.getOutputStream());
            }

            baseRequest.setHandled(true);
        }
    }
}
