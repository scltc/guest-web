package com.brickintellect.exhibit;

import java.io.File;
import java.io.IOException;
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
import org.nanohttpd.router.RouterNanoHTTPD.UriRouter;

import com.brickintellect.webserver.Redirector;
import com.brickintellect.webserver.SSLServerSocketFactoryCreator;
import com.brickintellect.webserver.WebSocketSessionManager;

public class WebServer extends NanoWSD implements IHandler<IHTTPSession, Response> {

    public static class IndexRedirectHandler extends DefaultHandler {

        static final String uri = "/index.html";

        @Override
        public String getText() {
            return "<html><head><meta http-equiv='refresh' content='0; URL=./index.html'></head></html>";
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

    public static class StaticPageHandlerWithIndexRedirect extends StaticPageHandler {

        static final IndexRedirectHandler redirector = new IndexRedirectHandler();

        @Override
        public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {

            Response response = super.get(uriResource, urlParams, session);
            if (response.getStatus() == Status.NOT_FOUND) {
                response = redirector.get(uriResource, urlParams, session);
            }
            else {
                // 1 second * 60 * 60 * 24 = 86,400 = 24 hours.
                response.addHeader("Cache-Control", "max-age=86400, public");

            }
            return response;
        }
    }

    private static class HttpHandler extends UriRouter implements IHandler<IHTTPSession, Response> {

        public Response handle(IHTTPSession session) {
            return super.process(session);
        }
    }

    private Redirector redirector = null;

    public WebServer(String host, int port, File keyStore, File root) {
        super(host, port);

        System.out.println("WebServer()");

        // Create a HTTPS socket factory if a key store specified.
        try {
            SSLServerSocketFactory socketFactory = SSLServerSocketFactoryCreator.create(keyStore);
            if (socketFactory != null) {
                makeSecure(socketFactory, null);

                redirector = new Redirector(80, "https://home.scltc.club/index.html");
            }

        } catch (IOException ignored) {
        }

        // Add the icon MIME type so "favicon.ico" will cache correctly.
        mimeTypes().put("ico", "image/x-icon");

        HttpHandler router = new HttpHandler();

        // Default page root to current directory if root not configured.
        router.addRoute("/(.)+", 999000, StaticPageHandlerWithIndexRedirect.class,
                (root == null) ? new File(".").getAbsoluteFile() : root);
        router.setNotFoundHandler(IndexRedirectHandler.class);
        this.setHTTPHandler(router);

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