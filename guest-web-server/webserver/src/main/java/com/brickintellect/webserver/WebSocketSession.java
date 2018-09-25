package com.brickintellect.webserver;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.googlecode.jsonrpc4j.JsonRpcBasicServer;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.CloseCode;
import org.nanohttpd.protocols.websockets.WebSocket;
import org.nanohttpd.protocols.websockets.WebSocketFrame;

public class WebSocketSession extends WebSocket {

    private static final Logger LOG = Logger.getLogger(WebSocketSession.class.getName());

    private static final boolean debug = true;
    private final JsonRpcBasicServer rpcServer;

    public WebSocketSession(final IHTTPSession session, final Object service, final Class<?> serviceClass) {
        super(session);
        System.out.println("WebSocketSession()");
        rpcServer = new JsonRpcBasicServer(service, serviceClass);
    }

    @Override
    protected void onOpen() {
        System.out.println("WebSocketSession.OnOpen()");
    }

    @Override
    protected void onClose(final CloseCode code, final String reason, final boolean initiatedByRemote) {
        System.out.println("WebSocketSession.OnClose()");
        if (debug) {
            System.out.println("C [" + (initiatedByRemote ? "Remote" : "Self") + "] "
                    + (code != null ? code : "UnknownCloseCode[" + code + "]")
                    + (reason != null && !reason.isEmpty() ? ": " + reason : ""));
        }
    }

    @Override
    protected void onMessage(final WebSocketFrame requestFrame) {
        System.out.println("WebSocketSession.OnMessage() {");
        try {
            requestFrame.setUnmasked();

            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);

                System.out.println(new String(requestFrame.getBinaryPayload(), "UTF-8"));

                rpcServer.handleRequest(new ByteArrayInputStream(requestFrame.getBinaryPayload()), outputStream);

                outputStream.flush();

                requestFrame.setBinaryPayload(outputStream.toByteArray());

                System.out.println(new String(requestFrame.getBinaryPayload(), "UTF-8"));

            } catch (Exception exception) {
                System.out.println(exception.toString());
            } finally {
            }

            sendFrame(requestFrame);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("WebSocketSession.OnMessage() }");
    }

    @Override
    protected void onPong(WebSocketFrame pong) {
        if (debug) {
            System.out.println("P " + pong);
        }
    }

    @Override
    protected void onException(IOException exception) {
        WebSocketSession.LOG.log(Level.SEVERE, "exception occured", exception);
    }
}
