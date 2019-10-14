package com.brickintellect.exhibit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.googlecode.jsonrpc4j.JsonRpcParam;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD.ResponseException;
import org.nanohttpd.protocols.http.response.IStatus;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
// import org.nanohttpd.router.RouterNanoHTTPD.DefaultHandler;
// import org.nanohttpd.router.RouterNanoHTTPD.DefaultStreamHandler;
import org.nanohttpd.router.RouterNanoHTTPD.UriResource;
import org.nanohttpd.router.RouterNanoHTTPD.UriResponder;

import com.brickintellect.webserver.Json;
import com.brickintellect.webserver.Json.JsonException;
import com.brickintellect.webserver.Json.JsonType;
import com.brickintellect.webserver.HttpRouter;
import com.brickintellect.webserver.WebSocketJsonRpcClient;
import com.brickintellect.webserver.WebSocketJsonRpcServer;
import com.brickintellect.webserver.WebSocketSession;
import com.brickintellect.webserver.WebSocketSessionManager;

import com.brickintellect.exhibit.CatchAndThrowPlayController.CatchAndThrowPlayState;
import com.brickintellect.exhibit.CatchAndThrow;
import com.brickintellect.exhibit.HeadTurnerPlayController.HeadTurnerPlayState;

public class Exhibit {

    private static Exhibit exhibit = new Exhibit();

    private Settings settings = new Settings();

    private final List<HeadTurnerPlayController> heads = new ArrayList<HeadTurnerPlayController>();
    private final List<CatchAndThrowPlayController> catchers = new ArrayList<CatchAndThrowPlayController>();

    public Exhibit() {

        settings.catchAndThrow.add(new CatchAndThrow.Settings());
        settings.headTurner.add(new HeadTurner.Settings());

        for (CatchAndThrow.Settings setting : settings.catchAndThrow) {
            catchers.add(new CatchAndThrowPlayController(setting, (UUID guest, CatchAndThrowPlayState state) -> {
                WebSocketSession session = WebSocketSessionManager.getConnection(guest);
                if (session != null) {
                    ((Exhibit.WebSocketService) session).catcherChanged(state);
                }
            }));
        }

        for (HeadTurner.Settings setting : settings.headTurner) {
            heads.add(new HeadTurnerPlayController(setting, (UUID guest, HeadTurnerPlayState state) -> {
                WebSocketSession session = WebSocketSessionManager.getConnection(guest);
                if (session != null) {
                    ((Exhibit.WebSocketService) session).headsChanged(state);
                }
            }));
        }
    }

    public interface ICatchAndThrowService {
        public CatchAndThrowPlayState catcherAbandon(@JsonRpcParam("instance") int instance);

        public CatchAndThrowPlayState catcherOperate(@JsonRpcParam("instance") int instance,
                @JsonRpcParam("direction") int direction);

        public CatchAndThrowPlayState catcherReserve(@JsonRpcParam("instance") int instance);
    }

    public interface IHeadTurnerWebSocketService {
        public HeadTurnerPlayState headsAbandon(@JsonRpcParam("instance") int instance);

        public HeadTurnerPlayState headsOperate(@JsonRpcParam("instance") int instance,
                @JsonRpcParam("direction") int direction);

        public HeadTurnerPlayState headsReserve(@JsonRpcParam("instance") int instance);
    }

    public interface IWebSocketService extends ICatchAndThrowService, IHeadTurnerWebSocketService {
        int ping(@JsonRpcParam("value") int value) throws Exception;

        Settings getSettings();

        Settings setSettings(@JsonRpcParam("settings") Settings settings);

    }

    public static class WebService {

        public static class JsonHandlerException extends Exception {

            private static final long serialVersionUID = 1L;

            private int code;

            public int getStatus() {
                return code;
            }

            public int setStatus(IStatus status) {
                return code = status.getRequestStatus();
            }

            public JsonHandlerException(Exception exception, IStatus status) {
                super(exception);
                code = status.getRequestStatus();
            }

            public static JsonHandlerException create(Exception exception, IStatus status) {
                if (exception instanceof JsonHandlerException) {
                    JsonHandlerException result = (JsonHandlerException) exception;
                    result.setStatus(status);
                    return result;
                }
                return new JsonHandlerException(exception, status);
            }

            public static JsonHandlerException create(Exception exception) {
                if (exception instanceof JsonHandlerException) {
                    return (JsonHandlerException) exception;
                }
                return new JsonHandlerException(exception, Status.INTERNAL_ERROR);
            }
        }

        public static class JsonResponseWrapper {
            public int status = 0;
            public Object exception = null;
            public Object response = null;
            public JsonResponseWrapper(Object value) {
                if (value instanceof Exception) {

                }
            }
        }

        public static abstract class JsonHandler<T> implements UriResponder {

            protected abstract JsonType<T> getObjectJsonType();

            protected abstract T getObject(int index) throws Exception;

            protected abstract T setObject(int index, T value) throws Exception;

            public static final String MIME_JSON = "application/json";

            public String getMimeType() {
                return MIME_JSON;
            }

            private IStatus status = Status.OK;

            public IStatus getStatus() {
                return status;
            };

            protected void setStatus(IStatus value) {
                status = value;
            }

            public int getIndex(Map<String, String> urlParams) throws JsonHandlerException {
                try {
                    return (!urlParams.containsKey("index")) ? -1 : Integer.parseInt(urlParams.get("index"));
                }
                catch (Exception exception) {
                    throw new JsonHandlerException(exception, Status.BAD_REQUEST);
                }

            }

            public String getContent(IHTTPSession session) throws IOException, ResponseException {
                long contentLength = (!session.getHeaders().containsKey("content-length")) ? 0
                        : Integer.parseInt(session.getHeaders().get("content-length"));

                // http://stackoverflow.com/a/9133993/229631
                byte[] buffer = new byte[1024];
                int length;
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                while ((length = session.getInputStream().read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
                byte[] outputBytes = outputStream.toByteArray();
                // todo: this will break with size > int.max
                byte[] postBodyBytes = Arrays.copyOfRange(outputBytes, outputBytes.length - (int) contentLength,
                        outputBytes.length);
                return new String(postBodyBytes, StandardCharsets.UTF_8);
            }

            private T DecodeJsonRequest(String value) {
                T object;
                try {
                    object = Json.decodeString(value, getObjectJsonType());
                } catch (JsonException exception) {
                    setStatus(Status.BAD_REQUEST);
                    object = null;
                }
                return object;

            }

            private Response EncodeJsonResponse(Exception exception, IStatus status) {
                setStatus(Status.INTERNAL_ERROR);
                return Response.newFixedLengthResponse(getStatus(), MIME_JSON,
                        Json.encodeString(new JsonHandlerException(exception, status)));
            }

            private Response EncodeJsonResponse(Exception exception) {
                return EncodeJsonResponse(exception, Status.INTERNAL_ERROR);
            }

            private Response EncodeJsonResponse(T value) {
                try {
                    return Response.newFixedLengthResponse(getStatus(), getMimeType(), Json.encodeString(value));
                } catch (JsonException exception) {
                    return EncodeJsonResponse(exception);
                }
            }

            /*
             * public T DecodeJson(ObjectMapper mapper, String value) { try { return
             * mapper.writeValueAsString(value); } catch (JsonProcessingException exception)
             * { setStatus(Status.INTERNAL_ERROR); return exception.getMessage(); } }
             */

            public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
                try {
                    return EncodeJsonResponse(getObject(getIndex(urlParams)));
                }
                catch (Exception exception) {
                    return EncodeJsonResponse(JsonHandlerException.create(exception));
                }

            }

            public Response put(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
                try {
                    return EncodeJsonResponse(setObject(getIndex(urlParams), DecodeJsonRequest(getContent(session))));
                }
                catch (Exception exception) {
                    return EncodeJsonResponse(JsonHandlerException.create(exception));
                }
            }

            public Response post(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
                return get(uriResource, urlParams, session);
            }

            public Response delete(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
                return get(uriResource, urlParams, session);
            }

            public Response other(String method, UriResource uriResource, Map<String, String> urlParams,
                    IHTTPSession session) {
                return get(uriResource, urlParams, session);
            }

        }

        public static class CatchAndThrowService extends JsonHandler<CatchAndThrow.Settings> {

            static JsonType<CatchAndThrow.Settings> jsonType = new JsonType<CatchAndThrow.Settings>();

            @Override
            protected JsonType<CatchAndThrow.Settings> getObjectJsonType() {
                return jsonType;
            }

            @Override
            public CatchAndThrow.Settings getObject(int index) throws Exception {
                if (index < 0) {
                    throw new Exception("booga booga");
                }
                return new CatchAndThrow.Settings(index);
            }

            @Override
            public CatchAndThrow.Settings setObject(int index, CatchAndThrow.Settings value) {
                return value;
            }
        }

        public static class HeadTurnerService extends JsonHandler<HeadTurner.Settings> {

            static JsonType<HeadTurner.Settings> jsonType = new JsonType<HeadTurner.Settings>();

            @Override
            protected JsonType<HeadTurner.Settings> getObjectJsonType() {
                return jsonType;
            }

            @Override
            public HeadTurner.Settings getObject(int index) {
                return new HeadTurner.Settings(index);
            }

            @Override
            public HeadTurner.Settings setObject(int index, HeadTurner.Settings value) {
                return value;
            }
        }

        public static void addRoutes(HttpRouter router) {
            router.addRoute("/api/heads/:index", 100, HeadTurnerService.class);
            router.addRoute("/api/catchers/:index", 100, CatchAndThrowService.class);
        }
    }

    public static class WebSocketService extends WebSocketSession implements IWebSocketService {

        private final WebSocketJsonRpcClient client;

        public WebSocketService(IHTTPSession session) {
            super(session);
            client = new WebSocketJsonRpcClient(this, 2, Json.mapper());
            addEndpoint(new WebSocketJsonRpcServer(this, 1, Json.mapper(), this, Exhibit.IWebSocketService.class));
            addEndpoint(client);
        }

        /*
         * public String connect() { System.out.println("connect()"); return
         * getClientIdentifier().toString(); }
         * 
         * public void disconnect() { System.out.println("disconnect()"); }
         */

        public int sendPing(int value) {
            try {
                return client.invoke("ping", value, int.class);
            } catch (Throwable exception) {
                System.out.println(exception.getMessage());
                return -1;
            }
        }

        public int ping(int value) throws Exception {
            /*
             * String result = "ping received: " + value; if (value % 10 == 0) {
             * sendPing(value); throw new Exception("ping # " + value + " failed.");
             * 
             * } System.out.println(result);
             */
            return value;
        }

        public Settings getSettings() {
            return exhibit.settings;
        }

        public Settings setSettings(Settings settings) {
            return exhibit.settings = settings;
        }

        // ICatchAndThrowService implementation.

        public CatchAndThrowPlayState catcherAbandon(int instance) {
            System.out.println("catcherAbandon");
            try {
                return exhibit.catchers.get(instance).abandon(client.getClientIdentifier());
            } catch (IndexOutOfBoundsException ignored) {
                return null;
            }
        }

        public CatchAndThrowPlayState catcherOperate(int instance, int direction) {
            System.out.println("catcherOperate");
            try {
                return exhibit.catchers.get(instance).operate(client.getClientIdentifier());
            } catch (IndexOutOfBoundsException ignored) {
                return null;
            }
        }

        public CatchAndThrowPlayState catcherReserve(int instance) {
            System.out.println("catcherReserve");
            try {
                return exhibit.catchers.get(instance).reserve(client.getClientIdentifier());
            } catch (IndexOutOfBoundsException ignored) {
                return null;
            }
        }

        public void catcherChanged(CatchAndThrowPlayState state) {
            System.out.println("catcherChanged");
            try {
                client.invoke("catcherChanged", state);
            } catch (Throwable exception) {
                System.out.println("catcherChanged exception: " + exception.getMessage());
            }
        }

        // IHeadTurnerWebSocketService implementation.

        public HeadTurnerPlayState headsAbandon(int instance) {
            System.out.println("headsAbandon");
            try {
                return exhibit.heads.get(instance).abandon(client.getClientIdentifier());
            } catch (IndexOutOfBoundsException ignored) {
                return null;
            }

        }

        public HeadTurnerPlayState headsOperate(int instance, int direction) {
            System.out.println("headsOperate");
            try {
                return exhibit.heads.get(instance).operate(client.getClientIdentifier(), direction);
            } catch (IndexOutOfBoundsException ignored) {
                return null;
            }
        }

        public HeadTurnerPlayState headsReserve(int instance) {
            System.out.println("headsReserve");
            try {
                return exhibit.heads.get(instance).reserve(client.getClientIdentifier());
            } catch (IndexOutOfBoundsException ignored) {
                return null;
            }
        }

        public void headsChanged(HeadTurnerPlayState state) {
            try {
                client.invoke("headsChanged", state);
            } catch (Throwable exception) {
                System.out.println("headsChanged exception: " + exception.getMessage());
            }
        }
    }
}