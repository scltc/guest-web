package com.brickintellect.exhibit;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import javax.net.ssl.SSLServerSocketFactory;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.util.IHandler;
import org.nanohttpd.protocols.http.response.IStatus;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import org.nanohttpd.protocols.websockets.NanoWSD;
import org.nanohttpd.protocols.websockets.WebSocket;
import org.nanohttpd.router.RouterNanoHTTPD.DefaultHandler;
import org.nanohttpd.router.RouterNanoHTTPD.StaticPageHandler;
import org.nanohttpd.router.RouterNanoHTTPD.UriResource;

import com.brickintellect.webserver.HttpRouter;
import com.brickintellect.webserver.PortRedirector;
import com.brickintellect.webserver.SSLServerSocketFactoryCreator;
import com.brickintellect.webserver.WebSocketSessionManager;

public class WebServer extends NanoWSD implements IHandler<IHTTPSession, Response> {

    /*
    A UriResponder that always redirects to the "/index.html" page.
    */
    public static class IndexRedirectHandler extends DefaultHandler {

        static final String uri = "/index.html";

        @Override
        public String getText() {
            return "<html><head><meta http-equiv='refresh' content='0; URL=" + uri + "'></head></html>";
        }

        @Override
        public String getMimeType() {
            return NanoHTTPD.MIME_HTML;
        }

        @Override
        public IStatus getStatus() {
            return Status.OK;
        }

        @Override
        public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {

            Response response = Response.newFixedLengthResponse(getStatus(), getMimeType(), getText());
            response.addHeader("Cache-Control", "max-age=86400, public");
            // response.addHeader("Accept-Ranges", "bytes");
            // response.addHeader("Location", uri);
            return response;
        }
    }

    /*
    The standard NanoHTTPD StaticPageHandler is hard coded to use its built-in Error404UriHandler
    for not found pages.  This version permits a custom not found handler class to be specified
    (as the second initParameter value).
    */
    public static class StaticPageHandlerWithCustomNotFoundHandler extends StaticPageHandler {

        @Override
        public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {

            Response response = super.get(uriResource, urlParams, session);
            if (response.getStatus() == Status.NOT_FOUND) {
                UriResource notFoundUriResource = new UriResource(null, 100, uriResource.initParameter(1, Class.class));
                response = notFoundUriResource.process(urlParams, session);
            } else {
                // 24 hours = 1 second * 60 * 60 * 24 = 86,400.
                response.addHeader("Cache-Control", "max-age=86400, public");
            }
            return response;
        }
    }

    private HttpRouter router = new HttpRouter();
    private PortRedirector redirector = null;

    public WebServer(String host, int port, File keyStore, File root) {
        super(host, port);

        System.out.println("WebServer()");

        // Create a HTTPS socket factory if a key store specified.
        try {
            SSLServerSocketFactory socketFactory = SSLServerSocketFactoryCreator.create(keyStore);
            if (socketFactory != null) {
                makeSecure(socketFactory, null);

                String redirect = "https://" + InetAddress.getLocalHost().getHostName() + ".scltc.club/index.html";

                System.out.println(redirect);

                redirector = new PortRedirector(80, redirect);
            }

        } catch (IOException ignored) {
        }

        // Add the icon MIME type so "favicon.ico" will cache correctly.
        mimeTypes().put("ico", "image/x-icon");

        Exhibit.WebService.addRoutes(router);
        // Default static page root to current directory if root not configured.
        router.addRoute("/(.)+", 999000, StaticPageHandlerWithCustomNotFoundHandler.class,
                (root == null) ? new File(".").getAbsoluteFile() : root, IndexRedirectHandler.class);
        router.setNotFoundHandler(IndexRedirectHandler.class);
        this.setHTTPHandler(router);

    }

    public void addRoute(String url, Class<?> handler, Object... initParameter) {
        router.addRoute(url, 100, handler, initParameter);
    }

    @Override
    public void stop() {
        // Shutdown the port 80 redirector.
        if (redirector != null) {
            redirector.shutdown();
        }
        // Shutdown web socket manager and close all connected websockets.
        WebSocketSessionManager.shutdown();
        // Shutdown the web server.
        super.stop();
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession session) {
        System.out.println("WebServer.openWebSocket()");

        return WebSocketSessionManager.createSession(new Exhibit.WebSocketService(session));
    }
}