package com.brickintellect.webserver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.googlecode.jsonrpc4j.JsonRpcParam;

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

import com.brickintellect.exhibit.CatchAndThrow;
import com.brickintellect.exhibit.LatchingRelay;
import com.brickintellect.exhibit.TrackSwitch;
import com.brickintellect.exhibit.HeadTurner;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WebServer extends NanoWSD implements IHandler<IHTTPSession, Response> {

    private static CatchAndThrow trainRunner = null;

    public static class IndexRedirectHandler extends DefaultHandler {

        static final String uri = "/index.html";

        @Override
        public String getText() {
            return "<html><body>Redirected: <a href=\"" + uri + "\">" + uri + "</a></body></html>";
        }

        @Override
        public String getMimeType() {
            return NanoHTTPD.MIME_HTML;
        }

        @Override
        public IStatus getStatus() {
            return Status.REDIRECT;
        }

        @Override
        public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {

            Response response = Response.newFixedLengthResponse(getStatus(), getMimeType(), getText());
            response.addHeader("Accept-Ranges", "bytes");
            response.addHeader("Location", uri);
            return response;
        }
    }

    public static class StaticPageHandlerWithIndexRedirect extends StaticPageHandler {

        static final IndexRedirectHandler redirector = new IndexRedirectHandler();

        @Override
        public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {

            Response response = super.get(uriResource, urlParams, session);
            return (response.getStatus() == Status.NOT_FOUND) ? redirector.get(uriResource, urlParams, session)
                    : response;
        }
    }

    public static class CatchAndThrowHandler extends DefaultHandler {
        @Override
        public String getText() {
            return "OK";
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

            if (trainRunner != null) {
                trainRunner.go();
            }

            return Response.newFixedLengthResponse(getStatus(), getMimeType(), getText());
        }
    }

    public static class TestRelayHandler extends DefaultHandler {

        @Override
        public String getText() {
            return "";
        }

        public String getText(String port) {

            final LatchingRelay theRelay = new LatchingRelay(port.charAt(0));

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    theRelay.test(8);
                }
            });
            thread.start();
            return "<html><body><h2>RelayTest(" + port + ")</h2></body></html>";
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

            String port = urlParams.get("port");

            return Response.newFixedLengthResponse(getStatus(), getMimeType(), getText(port));
        }
    }

    public static class TestSwitchHandler extends DefaultHandler {

        @Override
        public String getText() {
            return "";
        }

        public String getText(String port, String type) {

            final TrackSwitch theSwitch = (type != null && type.toLowerCase().equals("house"))
                    ? new TrackSwitch.House(port.charAt(0))
                    : new TrackSwitch.Actuator(port.charAt(0));

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    theSwitch.test(8);
                }
            });
            thread.start();
            return "<html><body><h2>SwitchTest(" + type + ", " + port + ")</h2></body></html>";
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

            String port = urlParams.get("port");
            String type = urlParams.get("type");

            return Response.newFixedLengthResponse(getStatus(), getMimeType(), getText(port, type));
        }
    }

    private static class HttpHandler extends UriRouter implements IHandler<IHTTPSession, Response> {

        public Response handle(IHTTPSession session) {
            return super.process(session);
        }
    }

    public class Exhibit {

        public Settings settings = new Settings();

        public final ArrayList<HeadTurner> heads = new ArrayList<HeadTurner>();
        public final ArrayList<CatchAndThrow> catchers = new ArrayList<CatchAndThrow>();

        public Exhibit() {

            settings.catchAndThrow.add(new CatchAndThrow.Settings());
            settings.headTurner.add(new HeadTurner.Settings());

            for (CatchAndThrow.Settings setting : settings.catchAndThrow) {
                catchers.add(new CatchAndThrow(setting));
            }

            for (HeadTurner.Settings setting : settings.headTurner) {
                heads.add(new HeadTurner(setting));
            }
        }

    }

    public interface IWebSocketService {
        int ping(@JsonRpcParam(value = "value") int value) throws Exception;

        Settings getSettings();

        Settings setSettings(@JsonRpcParam(value = "settings") Settings settings);

        int getHeadsDirection(@JsonRpcParam(value = "index") int index);

        int setHeadsDirection(@JsonRpcParam(value = "index") int index,
                @JsonRpcParam(value = "direction") int direction);

        int runCatchAndThrow(@JsonRpcParam(value = "index") int index);
    }

    public class WebSocketService implements IWebSocketService {

        private final WebSocketJsonRpcClient client;
        private final Exhibit exhibit;

        public WebSocketService(final WebSocketJsonRpcClient client, final Exhibit exhibit) {
            this.client = client;
            this.exhibit = exhibit;
        }

        public int sendPing(int value) {
            try {
                return client.invoke("ping", value, int.class);
            }
            catch (Throwable exception) {
                System.out.println(exception.getMessage());
                return -1;
            }
        }

        public int ping(int value) throws Exception {
            String result = "ping received: " + value;
            if (value % 10 == 0) {
                sendPing(value);
                throw new Exception("ping # " + value + " failed.");

            }
            System.out.println(result);
            return value;
        }

        public Settings getSettings() {
            return exhibit.settings;
        }

        public Settings setSettings(Settings settings) {
            return exhibit.settings = settings;
        }

        public int getHeadsDirection(int index) {
            return (index >= exhibit.heads.size()) ? 0 : exhibit.heads.get(index).getDirection();
        }

        public int setHeadsDirection(int index, int direction) {
            return (index >= exhibit.heads.size()) ? 0 : exhibit.heads.get(index).setDirection(direction);
        }

        public int runCatchAndThrow(int index) {
            return (index >= exhibit.catchers.size()) ? 0 : exhibit.catchers.get(index).go();
        }
    }

    final Exhibit exhibit;
    final HttpHandler router = new HttpHandler();

    public WebServer(String host, int port) {
        super(host, port);
        System.out.println("WebServer()");
        // We can provide only basic functionality when running on Windows.
        exhibit = (System.getProperty("os.name").toLowerCase().startsWith("windows")) ? null : new Exhibit();
    }

    public void start(File root) throws IOException {

        System.out.println("WebServer.start() {");

        router.addRoute("/test/relay/:port", 100, TestRelayHandler.class);
        router.addRoute("/test/switch/:port/:type", 100, TestSwitchHandler.class);
        router.addRoute("/catchthrow", 100, CatchAndThrowHandler.class);
        // Default page root to current directory if root not configured.
        router.addRoute("/(.)+", 999000, StaticPageHandlerWithIndexRedirect.class,
                (root == null) ? new File(".").getAbsoluteFile() : root);
        router.setNotFoundHandler(IndexRedirectHandler.class);

        super.setHTTPHandler(router);
        // The default NanoHTTPD socket read timeout is 5 seconds. While that
        // may be fine for a HTTP server, it doesn't work so well when serving
        // WebSockets. Our client "pings" every 5 seconds, three times that
        // period should work better.
        super.start(15 * 1000/* NanoHTTPD.SOCKET_READ_TIMEOUT */, false);

        // trainRunner = new CatchAndThrow();
        System.out.println("WebServer.start() }");
    }

    // @Override
    public Response xhandle(IHTTPSession session) {
        System.out.println("WebServer.handle()");

        return super.handle(session);

        // return router.process(session);
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected WebSocket openWebSocket(IHTTPSession session) {
        System.out.println("WebServer.openWebSocket()");

        WebSocketSession webSocketSession = WebSocketSessionManager.CreateSession(session); //new WebSocketSession(session);
        WebSocketJsonRpcClient client = new WebSocketJsonRpcClient(webSocketSession, 2, objectMapper);
        webSocketSession.addEndpoint(new WebSocketJsonRpcServer(webSocketSession, 1, objectMapper, new WebSocketService(client, exhibit), IWebSocketService.class));
        webSocketSession.addEndpoint(client);

        return webSocketSession;
    }

    protected Response addCORSHeaders(Map<String, String> queryHeaders, Response resp, String cors) {
        resp.addHeader("Access-Control-Allow-Origin", cors);
        resp.addHeader("Access-Control-Allow-Headers", calculateAllowHeaders(queryHeaders));
        resp.addHeader("Access-Control-Allow-Credentials", "true");
        resp.addHeader("Access-Control-Allow-Methods", ALLOWED_METHODS);
        resp.addHeader("Access-Control-Max-Age", "" + MAX_AGE);

        return resp;
    }

    private String calculateAllowHeaders(Map<String, String> queryHeaders) {
        // here we should use the given asked headers
        // but NanoHttpd uses a Map whereas it is possible for requester to send
        // several time the same header
        // let's just use default values for this version
        return System.getProperty(ACCESS_CONTROL_ALLOW_HEADER_PROPERTY_NAME, DEFAULT_ALLOWED_HEADERS);
    }

    private final static String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS, HEAD";

    private final static int MAX_AGE = 42 * 60 * 60;

    // explicitly relax visibility to package for tests purposes
    public final static String DEFAULT_ALLOWED_HEADERS = "origin,accept,content-type";

    public final static String ACCESS_CONTROL_ALLOW_HEADER_PROPERTY_NAME = "AccessControlAllowHeader";
}